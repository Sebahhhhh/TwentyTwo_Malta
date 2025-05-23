package drinkechat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import drinkechat.Messaggio.TipoMessaggio;

// gestisce la comunicazione con un singolo client.
// Si occupa di ricevere e inviare messaggi, gestire gli ordini e la chat.

public class ClienteChat {
    // parametri per la connessione al server
    private static final String HOST = "localhost";
    private static final int PORTA = 5000;

    // componenti per la connessione
    private Socket socket;
    private BufferedReader lettoreServer;
    private PrintWriter scrittoreServer;
    private BufferedReader lettoreConsole;

    private boolean attivo;
    private String nomeUtente;

    // avvia il client
    public void avvia() {
        // client come attivo
        attivo = true;

        try {
            // connessione al server
            System.out.println("\nTentativo di connessione a TwentyTwoMalta...");
            socket = new Socket(HOST, PORTA);
            System.out.println("Connessione stabilita con il server!");

            // inizializzazione dei lettori e scrittori per la comunicazione
            lettoreServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            scrittoreServer = new PrintWriter(socket.getOutputStream(), true);
            lettoreConsole = new BufferedReader(new InputStreamReader(System.in));

            // classe interfaccia sotto
            mostraInterfacciaGrafica();

            // avvia un thread separato per leggere i messaggi dal server
            Thread threadLettura = new Thread(this::leggiDalServer);
            threadLettura.setDaemon(true); // Termina quando il thread principale termina
            threadLettura.start();

            // gestisce l'input dell'utente nel thread principale
            gestisciInput();

        } catch (IOException e) {
            // errore
            System.err.println("\nErrore di connessione: " + e.getMessage());
            System.out.println("\nAssicurati che il server sia in esecuzione prima di avviare il client!");
            System.out.println("\nDevi avviare prima il server e poi il client.");
        } finally {
            chiudi();
        }
    }

    // l'interfaccia ""grafica""
    private void mostraInterfacciaGrafica() {
        System.out.println("\n╔════════════════════════════════════════════╗");
        System.out.println("║      TwentyTwoMalta Drink & Chat           ║");
        System.out.println("║               Client v2.0                  ║");
        System.out.println("║          (Comunicazione JSON)              ║");
        System.out.println("╚════════════════════════════════════════════╝");
    }
    // menu principale
    private void mostraMenu() {
        System.out.println("\nMenu Principale:");
        System.out.println("\n1) Visualizza i drink disponibili");
        System.out.println("2) Ordina un drink");
        System.out.println("3) Invia un messaggio");
        System.out.println("0) Esci dal locale");
        System.out.print("\nScegli un'opzione (0-3): ");
    }

    // l'input dell'utente
    private void gestisciInput() throws IOException {
        System.out.println("\nConnessione a TwentyTwoMalta in corso...");


        // per dare tempo al server di inviare il messaggio di benvenuto
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // nome utente
        System.out.print("\nInserisci il tuo nome: ");
        nomeUtente = lettoreConsole.readLine();

        if (nomeUtente == null || nomeUtente.trim().isEmpty()) {
            System.out.println("Nome utente non valido.");
            // se non mette nulla di base metto anonimo
            nomeUtente = "Anonimo";
        }

        // invia il nome al server
        Messaggio messaggio = new Messaggio(TipoMessaggio.NOME, nomeUtente);
        inviaMessaggioJSON(messaggio);

        System.out.println("\nRegistrazione utente in corso...");

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        //menu
        mostraMenu();

        //
        String linea;
        while (attivo && (linea = lettoreConsole.readLine()) != null) {


            // Controlla se il socket è chiuso o non connesso
            if (socket.isClosed() || !socket.isConnected()) {
                System.out.println("\n Connessione al server persa.");
                break;
            }

            try {
                int scelta = Integer.parseInt(linea.trim());
                switch (scelta) {
                    case 1:
                        inviaMessaggioJSON(new Messaggio(TipoMessaggio.LISTA, null));
                        break;

                    case 2:
                        System.out.print("Inserisci i numeri dei drink da ordinare (separati da virgola): ");
                        String numeriDrink = lettoreConsole.readLine();
                        Messaggio messaggioOrdine = new Messaggio(TipoMessaggio.ORDINA, numeriDrink.trim());
                        inviaMessaggioJSON(messaggioOrdine);
                        break;

                    case 3:
                        System.out.print("Inserisci il messaggio da inviare: ");
                        String testoChat = lettoreConsole.readLine();
                        Messaggio messaggioChat = new Messaggio(TipoMessaggio.CHAT, nomeUtente, testoChat);
                        inviaMessaggioJSON(messaggioChat);
                        break;

                    case 0:
                        inviaMessaggioJSON(new Messaggio(TipoMessaggio.ESCI, nomeUtente));
                        attivo = false;
                        break;

                    default:
                        System.out.println("\nOpzione non valida. Scegli un numero tra 0 e 3.");
                        mostraMenu();
                        break;
                }

                // mostra di nuovo il menu
                if (attivo && scelta != 0) {
                    mostraMenu();
                }
            } catch (NumberFormatException e) {
                System.out.println("\nDevi inserire un numero valido tra 0 e 3 per selezionare un'opzione.");
                mostraMenu();
            }
        }
    }

    // invia il messaggio JSON al server

    // Già spiegato in GestoreClient
    private void inviaMessaggioJSON(Messaggio messaggio) {
        if (scrittoreServer != null && attivo && socket != null && !socket.isClosed()) {
            try {
                String jsonMessaggio = JsonUtil.toJson(messaggio);
                jsonMessaggio = jsonMessaggio.replace("\n", " ").replace("\r", "");
                scrittoreServer.println(jsonMessaggio);
                scrittoreServer.flush();
            } catch (Exception e) {
                System.out.println("\nErrore durante la serializzazione del messaggio: " + e.getMessage());
            }
        } else {
            System.out.println("\nImpossibile inviare il messaggio: connessione chiusa");
            attivo = false;
        }
    }

    // legge i messaggi dal server
    private void leggiDalServer() {
        try {
            String jsonMessaggio;

            // legge continuamente messaggi dal server finché il client è attivo
            while (attivo && (jsonMessaggio = lettoreServer.readLine()) != null) {
                if (jsonMessaggio.trim().isEmpty()) {
                    continue;
                }

                try {
                    // controlla se il messaggio JSON è completo
                    if (!jsonMessaggio.endsWith("}")) {

                        // se messaggio è incompleto, legge piu righe
                        // fino a trovare la chiusura del messaggio
                        StringBuilder messaggioCompleto = new StringBuilder(jsonMessaggio);
                        String riga;
                        while ((riga = lettoreServer.readLine()) != null && !riga.contains("}")) {
                            messaggioCompleto.append(riga);
                        }

                        if (riga != null) {
                            messaggioCompleto.append(riga);
                        }
                        jsonMessaggio = messaggioCompleto.toString();
                    }

                    // deserializza il messaggio JSON
                    Messaggio messaggio = JsonUtil.fromJson(jsonMessaggio, Messaggio.class);

                    // controlla se il messaggio c'è o è vuoto
                    if (messaggio == null) {
                        System.out.println("ricevuto messaggio JSON non valido");
                        continue;
                    }

                    // processa il messaggio in base al suo tipo
                    switch (messaggio.getTipo()) {
                        case BENVENUTO:
                            System.out.println("\n " + messaggio.getContenuto());
                            break;

                        case INFO:
                            System.out.println("\n " + messaggio.getContenuto());
                            break;

                        case COMANDI:
                            System.out.println("\n" + messaggio.getContenuto());
                            break;

                        case ORDINE_RICEVUTO:
                            System.out.println("\n" + messaggio.getContenuto());
                            break;

                        case ORDINE_PRONTO:
                            System.out.println("\n" + messaggio.getContenuto() + " ");
                            break;

                        case CHAT:
                            String mittente = messaggio.getMittente();
                            String testoChat = messaggio.getContenuto();
                            if (mittente != null && !mittente.isEmpty()) {
                                System.out.println("\n" + mittente + ": " + testoChat);
                            } else {
                                System.out.println("\n" + testoChat);
                            }
                            break;

                        case LISTA_DRINK:
                            System.out.println("\n" + messaggio.getContenuto().trim());
                            break;

                        case NOTIFICA:
                            System.out.println("\n" + messaggio.getContenuto());
                            break;

                        case ERRORE:
                            System.out.println("\n" + messaggio.getContenuto());
                            break;

                        case DISCONNESSIONE:
                            System.out.println("\n" + messaggio.getContenuto());
                            attivo = false;
                            break;

                        default:
                            System.out.println("\n" + messaggio.getContenuto());
                    }

                    // mostra il menu dopo aver ricevuto un messaggio
                    if (attivo && messaggio.getTipo() != TipoMessaggio.BENVENUTO) {
                        System.out.print("\nScegli un'opzione (0-3): ");
                    }
                } catch (Exception e) {
                    System.out.println("\nErrore nella deserializzazione del messaggio: " + jsonMessaggio);
                    System.out.println("Dettaglio errore: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            // gestione errori durante la lettura dal server
            if (attivo) {
                System.err.println("\nErrore nella lettura dal server: " + e.getMessage());
            }
        } finally {
            // chiude le risorse in caso di uscita dal ciclo
            chiudi();
        }
    }

    // chiude il client
    private void chiudi() {
        // imposta il client come non attivo
        attivo = false;

        try {
            // chiude tutte le risorse di rete e I/O
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            if (lettoreServer != null) lettoreServer.close();
            if (scrittoreServer != null) scrittoreServer.close();
            if (lettoreConsole != null) lettoreConsole.close();

            System.out.println("\nConnessione chiusa.");
        } catch (IOException e) {
            // eventuali errori
            System.err.println("\nErrore durante la chiusura del client: " + e.getMessage());
        }
    }


    // main
    public static void main(String[] args) {
        // avvia il client
        ClienteChat client = new ClienteChat();
        client.avvia();
    }
}