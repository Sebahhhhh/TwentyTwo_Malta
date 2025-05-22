package drinkechat;

/**
 * Classe che rappresenta un messaggio JSON scambiato tra client e server.
 * Questa classe sostituisce il formato di comunicazione basato su stringhe.
 */

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

    // Costruttori
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