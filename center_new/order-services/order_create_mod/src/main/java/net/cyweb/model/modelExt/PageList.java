package net.cyweb.model.modelExt;

import lombok.Data;

import java.util.List;

@Data
public class PageList<T> {

    int totalNum;

    List<T> lists;

}
