package drinkechat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import drinkechat.Messaggio.TipoMessaggio;

// gestoreClient gestisce la comunicazione con un singolo client.
// si occupa di ricevere e inviare messaggi, gestire gli ordini e la chat.

public class GestoreClient implements Runnable {
    private Socket socketClient;
    private ServerLocale server;
    private GestoreOrdini gestoreOrdini;
    private BufferedReader lettore;
    private PrintWriter scrittore;
    private String nomeClient;
    private boolean attivo;

    // Lista dei drink disponibili
    private static final String[] DRINK_DISPONIBILI = {
            "Pina Colada", "Fruit Pina Colada", "Sex on The Beach", "Tequila Sunrise",
            "Blue Lagoon", "Blue Hawaiian", "Sea Breeze", "Wet Pussy", "Death By Sex",
            "Paloma", "Margarita", "Mojito", "Fruit Mojito", "Tom Collins", "Hangover",
            "Cosmopolitan", "Hurricane", "Long Island Iced Tea", "Adios Motherfucker",
            "Rocket Fuel", "Daiquiri", "Strawberry Mango"
    };

    // Array parallelo per i prezzi
    private static final double[] PREZZI_DRINK = {
            7.50, 5.00, 8.00, 6.50, 7.00, 7.00, 6.00, 5.50, 8.50,
            6.00, 7.50, 6.50, 6.50, 7.00, 5.00, 8.00, 7.50, 9.00,
            9.50, 6.50, 8.00, 7.00
    };

    // Costruttore del gestore client
    public GestoreClient(Socket socketClient, ServerLocale server, GestoreOrdini gestoreOrdini) {
        this.socketClient = socketClient;
        this.server = server;
        this.gestoreOrdini = gestoreOrdini;
        this.attivo = true;

        try {
            this.lettore = new BufferedReader(new InputStreamReader(socketClient.getInputStream()));
            this.scrittore = new PrintWriter(socketClient.getOutputStream(), true);

            Messaggio messaggioBenvenuto = new Messaggio(TipoMessaggio.BENVENUTO,
                    "Benvenuto a TwentyTwoMalta! Inserisci il tuo nome:");
            inviaMessaggio(messaggioBenvenuto);

        } catch (IOException e) {
            System.err.println("Errore durante l'inizializzazione del client: " + e.getMessage());
            attivo = false;
        }
    }

    // Avvia il thread per la gestione del client
    @Override
    public void run() {
        try {
            String jsonMessaggio;

            while (attivo && (jsonMessaggio = lettore.readLine()) != null) {
                elaboraMessaggioJSON(jsonMessaggio);
            }
        } catch (IOException e) {
            if (nomeClient != null) {
                System.err.println("Errore di comunicazione con il client " + nomeClient + ": " + e.getMessage());
            } else {
                System.err.println("Errore di comunicazione con un client: " + e.getMessage());
            }
        } finally {
            chiudi();
        }
    }

    // Elabora il messaggio JSON ricevuto dal client
    private void elaboraMessaggioJSON(String jsonMessaggio) {
        try {
            Messaggio messaggio = JsonUtil.fromJson(jsonMessaggio, Messaggio.class);

            if (messaggio == null) {
                inviaErrore("Formato messaggio non valido");
                return;
            }

            switch (messaggio.getTipo()) {
                case NOME:
                    registraNomeUtente(messaggio.getContenuto());
                    break;
                case ORDINA:
                    elaboraOrdine(messaggio.getContenuto());
                    break;
                case LISTA:
                    inviaListaDrink();
                    break;
                case ESCI:
                    disconnetti();
                    break;
                default:
                    inviaErrore("Comando non riconosciuto. Usa il menu numerico da 0 a 3.");
            }
        } catch (Exception e) {
            inviaErrore("Errore nell'elaborazione del messaggio: " + e.getMessage());
        }
    }

    // Registra il nome utente e invia messaggi di benvenuto
    private void registraNomeUtente(String nome) {
        this.nomeClient = nome;

        inviaMessaggio(new Messaggio(TipoMessaggio.INFO, "Connesso come: " + nomeClient));

        String comandiInfo = "Comandi disponibili:\n" +
                "\n1) Visualizza i drink disponibili\n" +
                "2) Ordina un drink\n" +
                "0) Esci dal locale";
        inviaMessaggio(new Messaggio(TipoMessaggio.COMANDI, comandiInfo));
    }

    // Elabora l'ordine del drink
    private void elaboraOrdine(String input) {
        if (input != null && !input.isEmpty()) {
            String[] inputDrinks = input.trim().split(",");
            double prezzoTotale = 0.0;
            StringBuilder drinksConfermati = new StringBuilder();

            for (String inputDrink : inputDrinks) {
                try {
                    int numeroDrink = Integer.parseInt(inputDrink.trim());
                    if (numeroDrink >= 1 && numeroDrink <= DRINK_DISPONIBILI.length) {
                        String nomeDrink = DRINK_DISPONIBILI[numeroDrink - 1];
                        double prezzoDrink = PREZZI_DRINK[numeroDrink - 1];
                        prezzoTotale += prezzoDrink;
                        drinksConfermati.append(nomeDrink).append(", ");
                    } else {
                        inviaErrore("Numero drink non valido: " + numeroDrink);
                    }
                } catch (NumberFormatException e) {
                    inviaErrore("Inserisci numeri separati da virgole per ordinare più drink.");
                }
            }

            if (drinksConfermati.length() > 0) {
                drinksConfermati.setLength(drinksConfermati.length() - 2);

                // Verifica se lo sconto è applicabile
                boolean scontoApplicato = gestoreOrdini.incrementaOrdini();
                if (scontoApplicato) {
                    prezzoTotale *= 0.8;
                }

                Messaggio messaggioOrdineRicevuto = new Messaggio(
                        TipoMessaggio.ORDINE_RICEVUTO,
                        "Ordine ricevuto: " + drinksConfermati +
                                ". Prezzo totale: €" + String.format("%.2f", prezzoTotale) +
                                (scontoApplicato ? " (Sconto del 20%)" : "")
                );
                inviaMessaggio(messaggioOrdineRicevuto);
            }
        } else {
            inviaErrore("Specifica il numero dei drink da ordinare, separati da una virgola.");
        }
    }

    // Invia la lista dei drink disponibili al client
    private void inviaListaDrink() {
        StringBuilder listaFormattata = new StringBuilder("Drink disponibili a TwentyTwoMalta:\n");
        for (int i = 0; i < DRINK_DISPONIBILI.length; i++) {
            listaFormattata.append(i + 1).append(". ")
                    .append(DRINK_DISPONIBILI[i])
                    .append(" (€").append(PREZZI_DRINK[i]).append(")\n");
        }
        listaFormattata.append("\nPer ordinare, seleziona l'opzione 2 e poi inserisci i numeri dei drink separati da una virgola.");

        Messaggio messaggioListaDrink = new Messaggio(
                TipoMessaggio.LISTA_DRINK,
                listaFormattata.toString()
        );
        inviaMessaggio(messaggioListaDrink);
    }

    private void inviaErrore(String messaggio) {
        Messaggio messaggioErrore = new Messaggio(
                TipoMessaggio.ERRORE,
                " " + messaggio
        );
        inviaMessaggio(messaggioErrore);
    }

    public void inviaMessaggio(Messaggio messaggio) {
        if (scrittore != null && attivo) {
            try {
                String jsonMessaggio = JsonUtil.toJson(messaggio);
                jsonMessaggio = jsonMessaggio.replace("\n", " ").replace("\r", "");
                scrittore.println(jsonMessaggio);
                scrittore.flush();
            } catch (Exception e) {
                System.err.println("Errore: " + e.getMessage());
            }
        }
    }

    // Metodo per disconnettere il client
    private void disconnetti() {
        try {
            Messaggio messaggioDisconnessione = new Messaggio(
                    TipoMessaggio.DISCONNESSIONE,
                    "Disconnessione avvenuta con successo. Arrivederci!"
            );
            inviaMessaggio(messaggioDisconnessione);
        } catch (Exception e) {
            System.err.println("Errore durante l'invio del messaggio di disconnessione: " + e.getMessage());
        } finally {
            chiudi();
        }
    }

    // Metodo per chiudere le risorse del client
    public void chiudi() {
        attivo = false;
        try {
            if (lettore != null) lettore.close();
            if (scrittore != null) scrittore.close();
            if (socketClient != null && !socketClient.isClosed()) {
                socketClient.close();
            }
        } catch (IOException e) {
            System.err.println("Errore durante la chiusura delle risorse del client: " + e.getMessage());
        } finally {
            server.rimuoviClient(this);
        }
    }
}
