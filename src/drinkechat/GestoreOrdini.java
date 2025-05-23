package drinkechat;

// classe che gestisce gli ordini dei drink nel sistema.

public class GestoreOrdini {

    // contatore per gli ordini
    // codice identificativo
    private int contatore;

    // costruttore
    public GestoreOrdini() {
        // inizializzazione Conta a 0
        this.contatore = 0;
    }

    // Incrementa il contatore degli ordini e verifica se è applicabile uno sconto
    public boolean incrementaOrdini() {
        contatore++;
        // Stampa a che ordine siamo ora
        System.out.println("Ordine attuale: #" + contatore);
        if (contatore % 10 == 0) {
            System.out.println("Sconto del 20% applicato all'ordine #" + contatore);
            return true; // Sconto
        }
        return false; // No sconto
    }

    // Ordine Attuale
        public void stampaNumeroOrdineAttuale() {
            System.out.println("Numero ordine attuale: #" + contatore);
        }


    // crea un nuovo ordine nel sistema e restituisce il suo identificativo numerico.
    // @param nomeDrink =  Il nome del drink ordinato
    // @param nomeClient = Il nome del client che ha effettuato l'ordine
    // @return = Il numero identificativo dell'ordine

    public int nuovoOrdine(String nomeDrink, String nomeClient) {
        int numeroOrdine = contatore++;
        System.out.println("Nuovo ordine registrato: #" + numeroOrdine +
                " - " + nomeDrink + " per " + nomeClient);

        return numeroOrdine;
    }

   // segna che ha fatto l'ordine
    public void completaOrdine(int numeroOrdine) {
        System.out.println("Ordine completato: #" + numeroOrdine);
    }
}
