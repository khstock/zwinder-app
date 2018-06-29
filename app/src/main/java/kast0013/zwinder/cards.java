package kast0013.zwinder;

public class cards {
    private String userId;
    private String name;
    public cards (String userId, String name){
        this.userId = userId;
        this.name = name;
    }

    //Getter & Setter Methode für die UserID
    public String getUserId(){
        return this.userId;
    }

    public void setUserId(String userId){
        this.userId = userId;
    }

    //Getter & Setter Methode für den Namen
    public String getName(){
        return this.name;
    }

    public void setName(String name){
        this.name = name;
    }

}
