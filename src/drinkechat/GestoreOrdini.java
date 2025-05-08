package drinkechat;

// Classe che gestisce tutti gli ordini di drink e monitora il loro stato.

public class GestoreOrdini {
    // Contatore per gli ordini
    private int contatore;

    // Costruttore che inizializza il contatore degli ordini.
    public GestoreOrdini() {
        // Inizializzazione Conta a 1
        this.contatore = 1;
    }

    // Crea un nuovo ordine nel sistema e restituisce il suo identificativo numerico.
    public int nuovoOrdine(String nomeDrink, String nomeClient) {
        int numeroOrdine = contatore++;
        System.out.println("📝 Nuovo ordine registrato: #" + numeroOrdine + 
                           " - " + nomeDrink + " per " + nomeClient);

        // Restituisce il numero dell'ordine
        return numeroOrdine;
    }

    // Ordine Completato
    public void completaOrdine(int numeroOrdine) {
        System.out.println("✅ Ordine completato: #" + numeroOrdine);
    }
}
