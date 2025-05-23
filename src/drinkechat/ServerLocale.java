package drinkechat;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

// server che gestisce le connessioni dei client, gli ordini di drink e la chat.
public class ServerLocale {
    // porta su cui il server ascolta le connessioni
    private static final int PORTA = 5000;
    private ServerSocket serverSocket;
    private GestoreOrdini gestoreOrdini;
    private List<GestoreClient> listaClient;
    private boolean attivo;

    // contruttore
    public ServerLocale() {
        this.gestoreOrdini = new GestoreOrdini();
        this.listaClient = new ArrayList<>();
        this.attivo = true;
    }

    // metodo per avviare il server
    public void avvia() {
        try {
            serverSocket = new ServerSocket(PORTA);
            // parte ""grafrica""
            System.out.println("┌─────────────────────────────────────────┐");
            System.out.println("│      TwentyTwoMalta Drink & Chat        │");
            System.out.println("│             Version  2.0                │");
            System.out.println("└─────────────────────────────────────────┘");
            System.out.println("Server avviato sulla porta: " + PORTA);
            System.out.println("In attesa di connessioni dai client...");

            // ciclo principale del server
            while (attivo) {
                try {
                    Socket socketClient = serverSocket.accept();
                    System.out.println("✓ Nuovo client connesso: " + socketClient.getInetAddress().getHostAddress());

                    // crea un nuovo gestore client per gestire la connessione
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


    // invia un messaggio a tutti i client tranne al mittente.
    // @param messaggio = Il messaggio da inoltrare
    // @param mittente = Il gestore client che ha inviato il messaggio

    public void inoltraMessaggioChat(Messaggio messaggio, GestoreClient mittente) {
        for (GestoreClient client : listaClient) {
            if (client != mittente) {
                client.inviaMessaggio(messaggio);
            }
        }
    }

    // rimuove un client dalla lista dei client connessi.
    // @param client = Il client da rimuovere

    public void rimuoviClient(GestoreClient client) {
        listaClient.remove(client);
        System.out.println("Client disconnesso. Client attivi: " + listaClient.size());
    }

    // chiude il server e tutti i client connessi.
    public void chiudi() {
        attivo = false;

        // chiude tutti i client connessi (presenti nella lista)
        for (GestoreClient client : new ArrayList<>(listaClient)) {
            client.chiudi();
        }

        // chiude il server socket
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
                System.out.println("Server chiuso correttamente");
            }
        } catch (IOException e) {
            System.err.println("Errore durante la chiusura del server: " + e.getMessage());
        }
    }

    // main
    public static void main(String[] args) {
        ServerLocale server = new ServerLocale();
        server.avvia();
    }
}