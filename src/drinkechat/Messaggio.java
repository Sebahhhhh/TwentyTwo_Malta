package drinkechat;

// classe Messaggio
// rappresenta un messaggio scambiato tra client e server.
// contiene informazioni sul tipo di messaggio, il mittente, il contenuto e il destinatario.

public class Messaggio {

    // messaggi supportati

    public enum TipoMessaggio {
        BENVENUTO,
        INFO,
        COMANDI,
        NOME,
        LISTA,
        LISTA_DRINK,
        ORDINA,
        ORDINE_RICEVUTO,
        ORDINE_PRONTO,
        CHAT,
        NOTIFICA,
        ERRORE,
        DISCONNESSIONE,
        ESCI
    }

    private TipoMessaggio tipo;
    private String mittente;
    private String contenuto;
    private String destinatario;
    private int numeroOrdine;

    // costruttori
    public Messaggio() {
    }

    public Messaggio(TipoMessaggio tipo, String contenuto) {
        this.tipo = tipo;
        this.contenuto = contenuto;
    }

    public Messaggio(TipoMessaggio tipo, String mittente, String contenuto) {
        this.tipo = tipo;
        this.mittente = mittente;
        this.contenuto = contenuto;
    }

    public Messaggio(TipoMessaggio tipo, String contenuto, int numeroOrdine) {
        this.tipo = tipo;
        this.contenuto = contenuto;
        this.numeroOrdine = numeroOrdine;
    }

    // Getters e Setters
    public TipoMessaggio getTipo() {
        return tipo;
    }

    public void setTipo(TipoMessaggio tipo) {
        this.tipo = tipo;
    }

    public String getMittente() {
        return mittente;
    }

    public void setMittente(String mittente) {
        this.mittente = mittente;
    }

    public String getContenuto() {
        return contenuto;
    }

    public void setContenuto(String contenuto) {
        this.contenuto = contenuto;
    }

    public String getDestinatario() {
        return destinatario;
    }

    public void setDestinatario(String destinatario) {
        this.destinatario = destinatario;
    }

    public int getNumeroOrdine() {
        return numeroOrdine;
    }

    public void setNumeroOrdine(int numeroOrdine) {
        this.numeroOrdine = numeroOrdine;
    }

    // ToString
    @Override
    public String toString() {
        return "Messaggio{" +
                "tipo=" + tipo +
                ", mittente='" + mittente + '\'' +
                ", contenuto='" + contenuto + '\'' +
                ", destinatario='" + destinatario + '\'' +
                ", numeroOrdine=" + numeroOrdine +
                '}';
    }
}
