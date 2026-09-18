package es.upm.miw.devops.rest.dtos;

public class UserActiveDto {

    private String id;
    private Boolean active;

    public UserActiveDto() {
    }

    public UserActiveDto(String id, Boolean active) {
        this.id = id;
        this.active = active;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
