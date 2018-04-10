// https://stackoverflow.com/questions/42348140/jackson-choose-children-class-for-deserialization-depending-of-field-existence

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import com.fasterxml.jackson.core.JsonProcessingException;

public class ResponseDeserializer extends StdDeserializer<Response> {
    public ResponseDeserializer() {
        super(Response.class);
    }

    @Override
    public Response deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        JsonNode node = p.readValueAsTree();
        JsonNode code = node.findValue("code");
        Response result;
        if (code != null && !code.isNull()) {
            result = new TranslationResponse();
            ((TranslationResponse)result).setCode(code.asInt());
            ((TranslationResponse)result).setLang(node.findValue("lang").asText());
            ((TranslationResponse)result).setText(new String[] {"tata"});
            
            for(final JsonNode objNode : node.findValue("text"))
            {
            	
            }
            
        } else {
            result = new ResizeResponse();
            ((ResizeResponse)result).setImg1(node.findValue("img1").asText());
            ((ResizeResponse)result).setImg2(node.findValue("img2").asText());
        }
        result.setOrig(node.findValue("orig").asText());
        return result;
    }
}
