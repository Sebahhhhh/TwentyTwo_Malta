package drinkechat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

// Client per la connessione al server
// Gestisce l'invio di ordini e la partecipazione alla chat.
public class ClienteChat {
    // Definizione dei parametri di connessione al server
    private static final String HOST = "localhost";
    private static final int PORTA = 5000;

    // Componenti di connessione e comunicazione
    private Socket socket;
    private BufferedReader lettoreServer;
    private PrintWriter scrittoreServer;
    private BufferedReader lettoreConsole;

    // Stato del client
    private boolean attivo;
    private String nomeUtente;

    // Avvia il client e gestisce la comunicazione con il server.
    public void avvia() {
        // Client come attivo
        attivo = true;

        try {
            // Connessione al server
            System.out.println("\nTentativo di connessione a TwentyTwoMalta...");
            socket = new Socket(HOST, PORTA);

            // Inizializzazione dei lettori e scrittori per la comunicazione
            lettoreServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            scrittoreServer = new PrintWriter(socket.getOutputStream(), true);
            lettoreConsole = new BufferedReader(new InputStreamReader(System.in));

            // Classe interfaccia sotto
            mostraInterfacciaGrafica();

            // Avvia un thread separato per leggere i messaggi dal server
            Thread threadLettura = new Thread(this::leggiDalServer);
            threadLettura.setDaemon(true); // Termina quando il thread principale termina
            threadLettura.start();

            // Gestisce l'input dell'utente nel thread principale
            gestisciInput();

        } catch (IOException e) {
            // Gestione dell'errore di connessione 
            System.err.println("\n❌ Errore di connessione: " + e.getMessage());
            System.out.println("\n⚠️ Assicurati che il server sia in esecuzione prima di avviare il client!");
            System.out.println("Devi avviare prima il server e poi il client.");
        } finally {
            // Chiusura delle risorse
            chiudi();
        }
    }

    // interfaccia
    private void mostraInterfacciaGrafica() {
        System.out.println("\n╔════════════════════════════════════════════╗");
        System.out.println("║      TwentyTwoMalta Drink & Chat           ║");
        System.out.println("║               Client v1.0                  ║");
        System.out.println("╚════════════════════════════════════════════╝");
    }

    // Mostra il menu principale delle opzioni disponibili
    private void mostraMenu() {
        System.out.println("\n📋 Menu Principale:");
        System.out.println("\n1) Visualizza i drink disponibili");
        System.out.println("2) Ordina un drink");
        System.out.println("3) Invia un messaggio");
        System.out.println("0) Esci dal locale");
        System.out.print("\nScegli un'opzione (0-3): ");
    }

    // Gestisce l'input dell'utente dalla console.
    private void gestisciInput() throws IOException {
        System.out.println("\n🌐 Connessione a TwentyTwoMalta in corso...");
        String linea;
        boolean primaInterazione = true;

        while (attivo && (linea = lettoreConsole.readLine()) != null) {
            if (primaInterazione) {
                // Prima interazione: registrazione del nome utente
                nomeUtente = linea;
                System.out.println("\n🔄 Registrazione utente in corso...");

                // Invia il nome al server
                scrittoreServer.println("NOME|" + nomeUtente);
                primaInterazione = false;
                mostraMenu();
            } else {
                // Accetta input per il menù
                try {
                    int scelta = Integer.parseInt(linea.trim());
                    switch (scelta) {
                        case 1: // Visualizza i drink disponibili
                            scrittoreServer.println("LISTA");
                            break;
                        case 2: // Ordina un drink
                            System.out.print("Inserisci il numero del drink da ordinare: ");
                            String numeroDrink = lettoreConsole.readLine();
                            try {
                                // Verifica che sia effettivamente un numero
                                Integer.parseInt(numeroDrink.trim());
                                scrittoreServer.println("ORDINA|" + numeroDrink.trim());
                            } catch (NumberFormatException e) {
                                // Errore se Input se il drink corrispondere al numero è valido
                                System.out.println("\n⚠️ Devi inserire un numero valido.");
                                mostraMenu();
                            }
                            break;
                        case 3: // Invia un messaggio alla chat
                            System.out.print("Inserisci il messaggio da inviare: ");
                            String messaggio = lettoreConsole.readLine();
                            scrittoreServer.println("CHAT|" + messaggio);
                            break;
                        case 0: // Esci dal locale (disconettiti dal server)
                            scrittoreServer.println("ESCI");
                            attivo = false;
                            break;
                        default:
                            // Opzione non valida
                            System.out.println("\n⚠️ Opzione non valida. Scegli un numero tra 0 e 3.");
                            mostraMenu();
                            break;
                    }
                    
                    // Mostra di nuovo il menu dopo ogni operazione (tranne uscita)
                    if (attivo && scelta != 0) {
                        mostraMenu();
                    }
                } catch (NumberFormatException e) {
                    System.out.println("\n❓ Devi inserire un numero valido tra 0 e 3 per selezionare un'opzione.");
                    mostraMenu();
                }
            }
        }
    }

    // Legge e visualizza i messaggi ricevuti dal server.
    private void leggiDalServer() {
        try {
            String messaggioServer;

            // Legge continuamente messaggi dal server finché il client è attivo
            while (attivo && (messaggioServer = lettoreServer.readLine()) != null) {
                // Estrai tipo e contenuto dal messaggio
                String[] parti = messaggioServer.split("\\|", 3);
                String tipo = parti[0];
                
                // Se non è in formato valido, mostra il messaggio grezzo
                if (parti.length < 2) {
                    System.out.println(messaggioServer);
                    continue;
                }
                
                // Estrai il messaggio
                String messaggio = parti[1];

                // Processa il messaggio in base al suo tipo
                switch (tipo) {
                    case "BENVENUTO":
                        // Messaggio di benvenuto dal server
                        System.out.println("\n👋 " + messaggio);
                        break;

                    case "INFO":
                        // Informazioni generali
                        System.out.println("\nℹ️ " + messaggio);
                        break;

                    case "COMANDI":
                        // Solamente "Comandi disponibili"
                        System.out.println("\n📋 Comandi disponibili:");
                        break;

                    case "ORDINE_RICEVUTO":
                        // Gestione degli ordini di drink (in preparazione)
                        System.out.println("\n⏳ " + messaggio);
                        break;
                        
                    case "ORDINE_PRONTO":
                        // Gestione degli ordini di drink (pronto)
                        System.out.println("\n🍹✨ " + messaggio + " ✨🍹");
                        break;

                    case "CHAT":
                        // Messaggio di chat da un altro utente
                        if (parti.length >= 3) {
                            String mittente = parti[1];
                            String testoChat = parti[2];
                            System.out.println("\n💬 " + mittente + ": " + testoChat);
                        } else {
                            System.out.println("\n💬 " + messaggio);
                        }
                        break;

                    case "LISTA_DRINK":
                        // Lista dei drink disponibili - formattazione speciale
                        System.out.println("\n" + messaggio.trim());
                        break;

                    case "NOTIFICA":
                        // Notifiche di sistema (entrate/uscite, eventi)
                        System.out.println("\n🔔 " + messaggio);
                        break;

                    case "ERRORE":
                        // Messaggi di errore
                        System.out.println("\n⚠️ " + messaggio);
                        break;

                    case "DISCONNESSIONE":
                        // Messaggio di disconnessione
                        System.out.println("\n👋 " + messaggio);
                        break;

                    default:
                        // Tipo di messaggio non riconosciuto
                        System.out.println("\n" + messaggio);
                }
                
                // Ripristina la visualizzazione dell'input dopo ogni messaggio ricevuto
                if (attivo && !tipo.equals("BENVENUTO")) {
                    System.out.print("\nScegli un'opzione (0-3): ");
                }
            }
        } catch (IOException e) {
            // Gestisce gli errori di comunicazione solo se il client è ancora attivo
            if (attivo) {
                System.err.println("\n❌ Errore nella lettura dal server: " + e.getMessage());
            }
        } finally {
            // Chiude le risorse in caso di uscita dal ciclo
            chiudi();
        }
    }

    // Chiude tutte le connessioni e le risorse del client.
    private void chiudi() {
        // Imposta il client come non attivo
        attivo = false;

        try {
            // Chiude tutte le risorse di rete e I/O
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            if (lettoreServer != null) lettoreServer.close();
            if (scrittoreServer != null) scrittoreServer.close();
            if (lettoreConsole != null) lettoreConsole.close();

            // Messaggio di conferma di chiusura
            System.out.println("\n🔌 Connessione chiusa.");
        } catch (IOException e) {
            // Gestione errori durante la chiusura
            System.err.println("\n❌ Errore durante la chiusura del client: " + e.getMessage());
        }
    }

    // Il main del client
    public static void main(String[] args) {
        // Crea e avvia un'istanza del client
        ClienteChat client = new ClienteChat();
        client.avvia();
    }
}
