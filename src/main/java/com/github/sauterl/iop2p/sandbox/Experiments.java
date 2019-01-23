package com.github.sauterl.iop2p.sandbox;

import com.github.sauterl.iop2p.ui.components.Markdown;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;

/**
 * TODO: write JavaDoc
 *
 * @author loris.sauter
 */
public class Experiments {

  public static void main(String[] args){

    String example = "*Ich* **bin** ein `Beispiel` für Markdown";

    // log was ein literal ist für jede methode


    // beispiel visitor
    Parser parser = Parser.builder().build();
    Node node = parser.parse(example);
    Markdown markdown = new Markdown();
    node.accept(markdown);

  }
}
