package drinkechat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

// Gestisce la comunicazione con un singolo client connesso al server.
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

    public GestoreClient(Socket socketClient, ServerLocale server, GestoreOrdini gestoreOrdini) {
        this.socketClient = socketClient;
        this.server = server;
        this.gestoreOrdini = gestoreOrdini;
        this.attivo = true;

        try {
            // Inizializza gli stream di input e output
            this.lettore = new BufferedReader(new InputStreamReader(socketClient.getInputStream()));
            this.scrittore = new PrintWriter(socketClient.getOutputStream(), true);

            // Richiede il nome utente al client
            inviaMessaggio("BENVENUTO|Benvenuto a TwentyTwoMalta! Inserisci il tuo nome:");

            // Legge il nome utente dal client
            String risposta = lettore.readLine();
            
            // Estrai il nome dalla risposta
            if (risposta.startsWith("NOME|")) {
                this.nomeClient = risposta.substring(5);
            } else {
                this.nomeClient = risposta;
            }

            // Invia informazioni iniziali al client
            inviaMessaggio("INFO|Connesso come: " + nomeClient);

            // Invia informazioni sui comandi disponibili
            inviaMessaggio("COMANDI|Comandi disponibili:\n" +
                "\n1) Visualizza i drink disponibili\n" +
                "2) Ordina un drink\n" +
                "3) Invia un messaggio\n" +
                "0) Esci dal locale");


            // In parole povere avvisa che c'è gente nuova collegata
            server.inoltraMessaggioChat(
                "NOTIFICA|" + nomeClient + " è entrato nel locale!",
                this
            );
        } catch (IOException e) {
            System.err.println("Errore durante l'inizializzazione del client: " + e.getMessage());
            attivo = false;
        }
    }

    @Override
    public void run() {
        try {
            String linea;
            // Ciclo principale per i messaggi dei clienti
            while (attivo && (linea = lettore.readLine()) != null) {
                elaboraMessaggio(linea);
            }
        } catch (IOException e) {
            System.err.println("Errore di comunicazione con il client " + nomeClient + ": " + e.getMessage());
        } finally {
            chiudi();
        }
    }

    // Elabora i messaggi ricevuti dal client.
    private void elaboraMessaggio(String messaggio) {
        // Supporto per messaggi
        String comando;
        String contenuto = "";
        
        // Estrai comando e contenuto
        if (messaggio.contains("|")) {
            String[] parti = messaggio.split("\\|", 2);
            comando = parti[0];
            if (parti.length > 1) {
                contenuto = parti[1];
            }
        } else {
            comando = messaggio;
        }

        // Elabora il comando ricevuto con switch-case
        switch (comando) {
            case "ORDINA":
                elaboraOrdine(contenuto);
                break;
            case "CHAT":
                elaboraChat(contenuto);
                break;
            case "LISTA":
                inviaListaDrink();
                break;
            case "ESCI":
                disconnetti();
                break;
            default:
                inviaErrore("Comando non riconosciuto. Usa il menu numerico da 0 a 3.");
        }
    }

    // Elabora un ordine di drink.
    private void elaboraOrdine(String input) {
        if (input != null && !input.isEmpty()) {
            // Variabile finale per il nome del drink
            String inputDrink = input.trim();
            String nomeDrinkFinale = null;

            try {
                // Prova a interpretare l'input come numero di drink
                int numeroDrink = Integer.parseInt(inputDrink);
                if (numeroDrink >= 1 && numeroDrink <= DRINK_DISPONIBILI.length) {
                    nomeDrinkFinale = DRINK_DISPONIBILI[numeroDrink - 1];
                } else {
                    inviaErrore("Numero drink non valido. I numeri validi sono da 1 a " + DRINK_DISPONIBILI.length);
                    return;
                }
            } catch (NumberFormatException e) {
                // Se non è un numero, invia un errore
                inviaErrore("Inserisci un numero valido di drink (1-" + DRINK_DISPONIBILI.length + ")");
                return;
            }

            // A questo punto nomeDrinkFinale contiene il nome del drink valido
            if (nomeDrinkFinale != null) {
                final String drinkDaOrdinare = nomeDrinkFinale;
                int numeroOrdine = gestoreOrdini.nuovoOrdine(drinkDaOrdinare, nomeClient);

                inviaMessaggio("ORDINE_RICEVUTO|Ordine ricevuto: " + drinkDaOrdinare + " - In preparazione");

                // Simulazione della preparazione dell'ordine in un thread separato
                new Thread(() -> {
                    try {
                        // Tempo di preparazione casuale (10+ secondi)
                        int tempoPreparazione = 10000 + (int)(Math.random() * 7500);
                        Thread.sleep(tempoPreparazione);

                        gestoreOrdini.completaOrdine(numeroOrdine);

                        // Drink fatto
                        inviaMessaggio("ORDINE_PRONTO|🍹 Ordine pronto: " + drinkDaOrdinare + " - Buona degustazione!");
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }).start();
            }
        } else {
            inviaErrore("Specifica il numero del drink da ordinare");
        }
    }

    // Elabora un messaggio di chat e lo inoltra agli altri client.
    private void elaboraChat(String testoChat) {
        if (testoChat != null && !testoChat.isEmpty()) {
            String messaggioDaInviare = "CHAT|" + nomeClient + "|" + testoChat;

            // Invia il messaggio a tutti gli altri client
            server.inoltraMessaggioChat(messaggioDaInviare, this);
            // Invia anche al mittente per conferma
            // Vedi il tuo stesso messaggio
            inviaMessaggio(messaggioDaInviare);
        } else {
            inviaErrore("Messaggio vuoto");
        }
    }

    // Invia la lista dei drink disponibili al client.
    private void inviaListaDrink() {
        StringBuilder listaFormattata = new StringBuilder("🍹 Drink disponibili a TwentyTwoMalta:\n");
        for (int i = 0; i < DRINK_DISPONIBILI.length; i++) {
            listaFormattata.append(i + 1).append(". ").append(DRINK_DISPONIBILI[i]).append("\n");
        }
        listaFormattata.append("\nPer ordinare, seleziona l'opzione 2 e poi inserisci il numero del drink.");

        inviaMessaggio("LISTA_DRINK|" + listaFormattata.toString());
    }

    // Gestisce la disconnessione del client.
    private void disconnetti() {
        inviaMessaggio("DISCONNESSIONE|Arrivederci! Grazie per aver visitato TwentyTwoMalta.");

        // Notifica gli altri client della disconnessione
        server.inoltraMessaggioChat(
            "NOTIFICA| " + nomeClient + " ha lasciato il locale.",
            this
        );
         // Lo disattiva
        attivo = false;
    }

    // Invia un messaggio di errore al client.
    private void inviaErrore(String messaggio) {
        inviaMessaggio("ERRORE|⚠️ " + messaggio);
    }

    // Invia un messaggio al client.
    public void inviaMessaggio(String messaggio) {
        if (scrittore != null && attivo) {
            scrittore.println(messaggio);
        }
    }

    // Chiude la connessione con il client.
    public void chiudi() {
        attivo = false;
        try {
            if (lettore != null) lettore.close();
            if (scrittore != null) scrittore.close();
            if (socketClient != null && !socketClient.isClosed()) {
                socketClient.close();
            }
        } catch (IOException e) {
            System.err.println("Errore durante la chiusura del client: " + e.getMessage());
        } finally {
            server.rimuoviClient(this);
        }
    }
}
