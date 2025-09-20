package jp.co.sss.efoot.form;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import org.springframework.validation.annotation.Validated;

/** フォーム（name属性に合わせたシンプル版） */
@Validated
public  class TopForm {
    @NotBlank
    @Size(max = 255)
    private String title;

    @NotNull
    private String type; // nationality | team | league

    // <input name="items"> を複数並べて送る想定
    private List<String> items = new ArrayList<>();

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public List<String> getItems() { return items; }
    public void setItems(List<String> items) { this.items = items; }
}
