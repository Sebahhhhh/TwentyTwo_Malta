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

    // lista dei drink disponibili (li ho presi veramente dalla lista dei drink disponibili a malta)
    private static final String[] DRINK_DISPONIBILI = {
            "Pina Colada", "Fruit Pina Colada", "Sex on The Beach", "Tequila Sunrise",
            "Blue Lagoon", "Blue Hawaiian", "Sea Breeze", "Wet Pussy", "Death By Sex",
            "Paloma", "Margarita", "Mojito", "Fruit Mojito", "Tom Collins", "Hangover",
            "Cosmopolitan", "Hurricane", "Long Island Iced Tea", "Adios Motherfucker",
            "Rocket Fuel", "Daiquiri", "Strawberry Mango"
    };

   // costruttore del gestore client
    public GestoreClient(Socket socketClient, ServerLocale server, GestoreOrdini gestoreOrdini) {
        this.socketClient = socketClient;
        this.server = server;
        this.gestoreOrdini = gestoreOrdini;
        this.attivo = true;

        try {
            // inizializza gli stream di input e output
            // bufferedReader per la lettura dei messaggi dal client
            // printWriter per l'invio dei messaggi al client

            this.lettore = new BufferedReader(new InputStreamReader(socketClient.getInputStream()));
            this.scrittore = new PrintWriter(socketClient.getOutputStream(), true);

            // richiede il nome utente al client
            Messaggio messaggioBenvenuto = new Messaggio(TipoMessaggio.BENVENUTO,
                    "Benvenuto a TwentyTwoMalta! Inserisci il tuo nome:");
            inviaMessaggio(messaggioBenvenuto);

        } catch (IOException e) {
            System.err.println("Errore durante l'inizializzazione del client: " + e.getMessage());
            attivo = false;
        }
    }

    // avvia il thread per la gestione del client
    @Override
    public void run() {
        try {
            String jsonMessaggio;

            // ciclo principale per i messaggi dei clienti
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

    // elabora il messaggio JSON ricevuto dal client
    private void elaboraMessaggioJSON(String jsonMessaggio) {
        try {
            // deserializza il messaggio JSON
            Messaggio messaggio = JsonUtil.fromJson(jsonMessaggio, Messaggio.class);

            if (messaggio == null) {
                inviaErrore("Formato messaggio non valido");
                return;
            }

            // elabora il messaggio in base al tipo (solo dopo che lo ha deserializzato)
            switch (messaggio.getTipo()) {
                case NOME:
                    registraNomeUtente(messaggio.getContenuto());
                    break;
                case ORDINA:
                    elaboraOrdine(messaggio.getContenuto());
                    break;
                case CHAT:
                    elaboraChat(messaggio);
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

    // registra il nome utente e invia messaggi di benvenuto
    private void registraNomeUtente(String nome) {
        this.nomeClient = nome;

        inviaMessaggio(new Messaggio(TipoMessaggio.INFO, "Connesso come: " + nomeClient));

        // invia i comandi disponibili
        String comandiInfo = "Comandi disponibili:\n" +
                "\n1) Visualizza i drink disponibili\n" +
                "2) Ordina un drink\n" +
                "3) Invia un messaggio\n" +
                "0) Esci dal locale";
        inviaMessaggio(new Messaggio(TipoMessaggio.COMANDI, comandiInfo));

        // se qualcuno si collega, notifica agli altri client
        Messaggio notificaNuovoUtente = new Messaggio(
                TipoMessaggio.NOTIFICA,
                nomeClient + " è entrato nel locale!"
        );
        server.inoltraMessaggioChat(notificaNuovoUtente, this);
    }

    // elabora l'ordine del drink
    private void elaboraOrdine(String input) {
        if (input != null && !input.isEmpty()) {
            // variabile finale per il nome del drink
            String inputDrink = input.trim();
            String nomeDrinkFinale = null;

            try {
                // controlla se l'input è un numero valido
                // se è un numero, lo converte in int e verifica se è compreso nell'intervallo dei drink disponibili
                // se non è un numero, lancia un'eccezione
                int numeroDrink = Integer.parseInt(inputDrink);
                if (numeroDrink >= 1 && numeroDrink <= DRINK_DISPONIBILI.length) {
                    nomeDrinkFinale = DRINK_DISPONIBILI[numeroDrink - 1];
                } else {
                    inviaErrore("Numero drink non valido. I numeri validi sono da 1 a " + DRINK_DISPONIBILI.length);
                    return;
                }
            } catch (NumberFormatException e) {
                inviaErrore("Inserisci un numero valido di drink (1-" + DRINK_DISPONIBILI.length + ")");
                return;
            }

            // a questo punto nomeDrinkFinale contiene il nome del drink valido
            // procede con l'ordine
            if (nomeDrinkFinale != null) {
                final String drinkDaOrdinare = nomeDrinkFinale;
                int numeroOrdine = gestoreOrdini.nuovoOrdine(drinkDaOrdinare, nomeClient);

                // crea e invia al client messaggio di conferma

                Messaggio messaggioOrdineRicevuto = new Messaggio(
                        TipoMessaggio.ORDINE_RICEVUTO,
                        "Ordine ricevuto: " + drinkDaOrdinare + " - In preparazione"
                );
                messaggioOrdineRicevuto.setNumeroOrdine(numeroOrdine);
                inviaMessaggio(messaggioOrdineRicevuto);

                // prepara l'ordine
                new Thread(() -> {
                    try {
                        // Tempo di preparazione random tra 10 e 18 secondi
                        int tempoPreparazione = 10000 + (int)(Math.random() * 8500);
                        Thread.sleep(tempoPreparazione);

                        gestoreOrdini.completaOrdine(numeroOrdine);

                        // crea e invia messaggio di ordine pronto
                        Messaggio messaggioOrdineProonto = new Messaggio(
                                TipoMessaggio.ORDINE_PRONTO,
                                "Ordine pronto: " + drinkDaOrdinare + " "
                        );
                        messaggioOrdineProonto.setNumeroOrdine(numeroOrdine);
                        inviaMessaggio(messaggioOrdineProonto);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }).start();
            }
        } else {
            inviaErrore("Specifica il numero del drink da ordinare");
        }
    }
   // ! CHAT !

    // inoltra il messaggio di chat a tutti gli altri client
    // e invia una copia al mittente

  // elabora il messaggio di chat
    private void elaboraChat(Messaggio messaggioChat) {
        String testoChat = messaggioChat.getContenuto();
        if (testoChat != null && !testoChat.isEmpty()) {

            // crea il messaggio da inviare
            Messaggio messaggioDaInviare = new Messaggio(
                    TipoMessaggio.CHAT,
                    nomeClient,
                    testoChat
            );

            // invia il messaggio a tutti gli altri client
            server.inoltraMessaggioChat(messaggioDaInviare, this);

            // invia anche al mittente
            inviaMessaggio(messaggioDaInviare);
        } else {
            // se vuoto da errore
            inviaErrore("Messaggio vuoto");
        }
    }

  // invia la lista dei drink disponibili al client

    private void inviaListaDrink() {
        StringBuilder listaFormattata = new StringBuilder(" Drink disponibili a TwentyTwoMalta:\n");
        for (int i = 0; i < DRINK_DISPONIBILI.length; i++) {
            listaFormattata.append(i + 1).append(". ").append(DRINK_DISPONIBILI[i]).append("\n");
        }
        listaFormattata.append("\nPer ordinare, seleziona l'opzione 2 e poi inserisci il numero del drink.");

        Messaggio messaggioListaDrink = new Messaggio(
                TipoMessaggio.LISTA_DRINK,
                listaFormattata.toString()
        );
        inviaMessaggio(messaggioListaDrink);
    }

   // disconnette il client

    private void disconnetti() {
        Messaggio messaggioArrivederci = new Messaggio(
                TipoMessaggio.DISCONNESSIONE,
                "Ciao."
        );
        inviaMessaggio(messaggioArrivederci);

        // dice ai cliente che il client si è disconnesso
        if (nomeClient != null) {
            Messaggio notificaUscita = new Messaggio(
                    TipoMessaggio.NOTIFICA,
                    nomeClient + " ha lasciato il locale."
            );
            server.inoltraMessaggioChat(notificaUscita, this);
        }


        attivo = false;
    }

    // errore
    private void inviaErrore(String messaggio) {
        Messaggio messaggioErrore = new Messaggio(
                TipoMessaggio.ERRORE,
                " " + messaggio
        );
        inviaMessaggio(messaggioErrore);
    }

   // invia un messaggio JSON al client

    public void inviaMessaggio(Messaggio messaggio) {
        if (scrittore != null && attivo) {
            try {
                    // converte l'oggetto Messaggio in una stringa JSON utilizzando la classe JsonUtil
                    String jsonMessaggio = JsonUtil.toJson(messaggio);

                    // rimuove eventuali caratteri di newline (\n) e ritorno carrello (\r) dalla stringa JSON
                    // questo previene problemi di parsing lato client poiché il protocollo usa readLine()
                    jsonMessaggio = jsonMessaggio.replace("\n", " ").replace("\r", "");

                    // invia la stringa JSON al client attraverso il PrintWriter,
                    scrittore.println(jsonMessaggio);

                    // forza lo svuotamento del buffer per assicurarsi che il messaggio venga mandato subito
                    scrittore.flush();
            } catch (Exception e) {
                System.err.println("Errore: " + e.getMessage());
            }
        }
    }

    // chiude la connessione con il client
    public void chiudi() {
        attivo = false;
        try {
            // chiude gli stream di input e output
            if (lettore != null) lettore.close();
            if (scrittore != null) scrittore.close();
            if (socketClient != null && !socketClient.isClosed()) {
                socketClient.close();
            }
        } catch (IOException e) {
            System.err.println("Errore: " + e.getMessage());
        } finally {
            server.rimuoviClient(this);
        }
    }
}
