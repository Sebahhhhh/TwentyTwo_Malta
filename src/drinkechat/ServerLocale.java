package drinkechat;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

// Server che gestisce le connessioni dei client, gli ordini di drink e la chat di gruppo.
public class ServerLocale {
    private static final int PORTA = 5000;
    private ServerSocket serverSocket;
    private GestoreOrdini gestoreOrdini;
    private List<GestoreClient> listaClient;
    private boolean attivo;

    public ServerLocale() {
        this.gestoreOrdini = new GestoreOrdini();
        this.listaClient = new ArrayList<>();
        this.attivo = true;
    }

    // Avvia il server e attende connessioni dai client.
    public void avvia() {
        try {
            serverSocket = new ServerSocket(PORTA);
            System.out.println("┌─────────────────────────────────────────┐");
            System.out.println("│      TwentyTwoMalta Drink & Chat        │");
            System.out.println("│             Version  1.1                │");
            System.out.println("└─────────────────────────────────────────┘");
            System.out.println("Server avviato sulla porta: " + PORTA);
            System.out.println("In attesa di connessioni dai client...");

            // Ciclo principale del server
            while (attivo) {
                try {
                    Socket socketClient = serverSocket.accept();
                    System.out.println("✓ Nuovo client connesso: " + socketClient.getInetAddress().getHostAddress());

                    // Crea un nuovo gestore per il client e lo avvia su un thread separato
                    GestoreClient gestoreClient = new GestoreClient(socketClient, this, gestoreOrdini);
                    listaClient.add(gestoreClient);
                    new Thread(gestoreClient).start();
                } catch (IOException e) {
                    if (!serverSocket.isClosed()) {
                        System.err.println("Errore durante l'accettazione di un client: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Errore all'avvio del server: " + e.getMessage());
        } finally {
            chiudi();
        }
    }

    // Invia un messaggio a tutti i client tranne al mittente.
    public void inoltraMessaggioChat(String messaggio, GestoreClient mittente) {
        for (GestoreClient client : listaClient) {
            if (client != mittente) {
                client.inviaMessaggio(messaggio);
            }
        }
    }

    // Rimuove un client dalla lista quando si disconnette.
    public void rimuoviClient(GestoreClient client) {
        listaClient.remove(client);
        System.out.println("✗ Client disconnesso. Client attivi: " + listaClient.size());
    }

    // Chiude il server e tutte le connessioni client.
    public void chiudi() {
        attivo = false;

        // Chiude tutti i client connessi (presenti nella lista)
        for (GestoreClient client : new ArrayList<>(listaClient)) {
            client.chiudi();
        }

        // Chiude il server socket
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
                System.out.println("Server chiuso correttamente");
            }
        } catch (IOException e) {
            System.err.println("Errore durante la chiusura del server: " + e.getMessage());
        }
    }

   // Banalmente il main che fa partire il programma
    public static void main(String[] args) {
        ServerLocale server = new ServerLocale();
        server.avvia();
    }
}
