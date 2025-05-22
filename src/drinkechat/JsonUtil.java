package drinkechat;

// jackson librerie
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;


  // classe di utilità per la serializzazione e deserializzazione JSON.
  // utilizza la libreria Jackson per convertire oggetti Java in stringhe JSON e viceversa.

public class JsonUtil {

    // istanza ObjectMapper configurata per output JSON leggibile (Cercata su google)
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .configure(SerializationFeature.INDENT_OUTPUT, false);

    // converte un oggetto Java in una stringa JSON

    public static String toJson(Object oggetto) {
        try {
            return objectMapper.writeValueAsString(oggetto);
        } catch (Exception e) {
            throw new RuntimeException("Errore nella serializzazione JSON: " + e.getMessage(), e);
        }
    }
    // converte una stringa JSON in un oggetto Java di tipo specificato
    // viceversa di quello sopra

    public static <T> T fromJson(String json, Class<T> classe) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        
        try {
            // controlla se il JSON è completo e ben formattato
            // se non è completo lancia un'eccezione
            if (!json.trim().startsWith("{") || !json.trim().endsWith("}")) {
                throw new JsonParseException(null, "JSON incompleto: " + json);
            }
            
            return objectMapper.readValue(json, classe);
        } catch (JsonParseException e) {
            System.err.println("Errore di parsing JSON: " + e.getMessage());
            System.err.println("JSON ricevuto: " + json);
            return null;
        } catch (Exception e) {
            throw new RuntimeException("Errore nella deserializzazione JSON: " + e.getMessage(), e);
        }
    }
}
