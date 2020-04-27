package gigaherz.enderRift.common;

import com.google.common.collect.Maps;
import com.google.gson.*;
import net.minecraft.util.JSONUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.function.BiConsumer;

public class Requirements
{
    public static final Logger LOGGER = LogManager.getLogger();

    public static final String WEARS_JSON = "F:/wears.json"; // FOR TESTING ONLY
    public static Requirements readFromFile()
    {
        try (
                InputStream input = new FileInputStream(WEARS_JSON);
                Reader reader = new BufferedReader(new InputStreamReader(input));
        )
        {
            return DESERIALIZER.fromJson(reader, Requirements.class);
        } catch (IOException e) {
            LOGGER.error("Could not parse json from {}", WEARS_JSON, e);

            // If couldn't read, just return an empty object. This may not be what you want.
            return new Requirements();
        }
    }

    public static class RequirementItem
    {
        private final Map<String, Integer> requirements = Maps.newHashMap();

        public int get(String requirement)
        {
            return requirements.get(requirement);
        }
    }

    private final Map<String, RequirementItem> wears = Maps.newHashMap();
    private final Map<String, RequirementItem> tools = Maps.newHashMap();
    private final Map<String, RequirementItem> weapons = Maps.newHashMap();
    private final Map<String, RequirementItem> xpValues = Maps.newHashMap();

    public int getWear(String item, String requirement)
    {
        return wears.get(item).get(requirement);
    }

    public int getTool(String item, String requirement)
    {
        return tools.get(item).get(requirement);
    }

    public int getWeapon(String item, String requirement)
    {
        return weapons.get(item).get(requirement);
    }

    public int getXp(String item, String requirement)
    {
        return xpValues.get(item).get(requirement);
    }

    // -----------------------------------------------------------------------------
    //
    // GSON STUFFS BELOW
    //
    //

    private static final Gson DESERIALIZER = new GsonBuilder()
            .registerTypeAdapter(Requirements.class, new Deserializer())
            .registerTypeAdapter(RequirementItem.class, new EntryDeserializer())
            .create();

    private static class Deserializer implements JsonDeserializer<Requirements>
    {

        @Override
        public Requirements deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
        {
            Requirements req = new Requirements();

            JsonObject obj = json.getAsJsonObject();
            deserializeGroup(obj, "wearRequirement", req.wears::put, context);
            deserializeGroup(obj, "toolRequirement", req.tools::put, context);
            deserializeGroup(obj, "weaponRequirement", req.weapons::put, context);
            deserializeGroup(obj, "xpValues", req.xpValues::put, context);

            return req;
        }

        private void deserializeGroup(JsonObject obj, String requirementGroupName, BiConsumer<String, RequirementItem> putter, JsonDeserializationContext context)
        {
            if (obj.has(requirementGroupName))
            {
                JsonObject wears = JSONUtils.getJsonObject(obj, requirementGroupName);
                for(Map.Entry<String, JsonElement> entries : wears.entrySet())
                {
                    String name = entries.getKey();
                    RequirementItem values = context.deserialize(entries.getValue(), RequirementItem.class);
                    putter.accept(name, values);
                }
            }
        }
    }

    private static class EntryDeserializer implements JsonDeserializer<RequirementItem>
    {

        @Override
        public RequirementItem deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
        {
            RequirementItem item = new RequirementItem();

            JsonObject obj = json.getAsJsonObject();
            for(Map.Entry<String, JsonElement> entries : obj.entrySet())
            {
                String name = entries.getKey();
                Integer values = entries.getValue().getAsInt();
                item.requirements.put(name, values);
            }

            return item;
        }
    }
}
