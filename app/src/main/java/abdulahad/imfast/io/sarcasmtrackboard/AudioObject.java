package abdulahad.imfast.io.sarcasmtrackboard;

public class AudioObject {

    private String itemName;
    private Integer itemId;

    public AudioObject(String itemName, Integer itemId) {

        this.itemName = itemName;
        this.itemId = itemId;
    }

    public String getItemName() {

        return itemName;
    }

    public Integer getItemId() {

        return itemId;
    }

}
