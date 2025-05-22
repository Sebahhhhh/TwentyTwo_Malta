package drinkechat;

// classe che gestisce gli ordini dei drink nel sistema.

public class GestoreOrdini {

    // contatore per gli ordini
    // codice identificativo
    private int contatore;

    // costruttore
    public GestoreOrdini() {
        // inizializzazione Conta a 1
        this.contatore = 1;
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