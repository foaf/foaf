package org.rdfweb.viz;

public interface FocussedItem {

    public void updateText(String s);

    public void updateType(String s);
    public void updateID(String s);
    public void updateLabel(String s);

    public String getTmpText();

}
