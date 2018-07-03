package kast0013.zwinder;

public class cards {
    private String userId;
    private String name;
    private String userImageUrl;
    public cards (String userId, String name, String url){
        this.userId = userId;
        this.name = name;
        this.userImageUrl = url;
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

    //Getter & Setter Methoden für die Profilbild URL
    public String getUserImageUrl(){
        return userImageUrl;
    }
    public void setUserImageUrl(String userImageUrl){
        this.userImageUrl = userImageUrl;
    }

}
