package com.github.sauterl.iop2p.ui.components;

import java.util.ArrayList;
import java.util.List;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextFlow;
import org.commonmark.node.AbstractVisitor;
import org.commonmark.node.Code;
import org.commonmark.node.Emphasis;
import org.commonmark.node.StrongEmphasis;
import org.commonmark.node.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Markdown extends AbstractVisitor {

  // TODO handle nested formats

  private static final Logger LOGGER = LoggerFactory.getLogger(Markdown.class);
  private FormatType activeNode = FormatType.PLAIN;
  private List<javafx.scene.text.Text> list = new ArrayList<>();
  private static Font italicFont;
  private static Font boldFont;
  private static Font monoFont;

  {
    Font font = Font.getDefault();
    italicFont = Font.font("Arial", FontPosture.ITALIC, font.getSize());
    boldFont = Font.font("Arial", FontWeight.BOLD, font.getSize());
    monoFont = Font.font("Courier", font.getSize());
  }

  private enum FormatType {
    EMPHASIS,
    STRONG_EMPHASIS,
    CODE,
    PLAIN
  }

  // emphasis
  @Override
  public void visit(Emphasis emphasis) {
    activeNode = FormatType.EMPHASIS;
    visitChildren(emphasis);
    activeNode = FormatType.PLAIN;
  }

  // strong emphasis
  @Override
  public void visit(StrongEmphasis strongEmphasis) {
    activeNode = FormatType.STRONG_EMPHASIS;
    visitChildren(strongEmphasis);
    activeNode = FormatType.PLAIN;
  }

  // code
  @Override
  public void visit(Code code) {
    activeNode = FormatType.CODE;
    String style = "-fx-wrap-text: true;-fx-font-family: monospace;-fx-padding: 0 8 0 8;";
    javafx.scene.text.Text t = new javafx.scene.text.Text(code.getLiteral());
    t.setStyle(style);
    list.add(t);
    visitChildren(code);
    activeNode = FormatType.PLAIN;
  }
  // text
  @Override
  public void visit(Text text) {
    Font font = null;
    String style = "-fx-wrap-text: true;";
    switch (activeNode) {
      case EMPHASIS:
        font = italicFont;
        style += "-fx-font-family: sans-serif; -fx-font-style: italic;";
        break;
      case STRONG_EMPHASIS:
        font = boldFont;
        style += "-fx-font-family: sans-serif; -fx-font-weight: bold;";
        break;
      case CODE:
        return;
        /*
        font = monoFont;
        style += "-fx-font-family: monospace; -fx-font-weight: bold; -fx-font-size: 16;-fx-padding: 0 8 0 8;";
        break;*/
      case PLAIN:
        font = Font.getDefault();
        break;
    }
    javafx.scene.text.Text t = new javafx.scene.text.Text(text.getLiteral());
    t.setStyle(style);
    LOGGER.debug("Visit Text: Active={}, literal={}", activeNode,text.getLiteral());
    //t.setFont(font); // not working, apparently
    list.add(t);
  }

  public TextFlow getTextflow() {
    return new TextFlow(list.toArray(new javafx.scene.text.Text[0]));
  }
}
