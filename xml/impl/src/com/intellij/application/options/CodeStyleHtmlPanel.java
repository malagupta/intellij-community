/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.application.options;

import com.intellij.application.options.codeStyle.RightMarginForm;
import com.intellij.ide.highlighter.XmlHighlighterFactory;
import com.intellij.openapi.application.ApplicationBundle;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.highlighter.EditorHighlighter;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.Comparing;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.psi.formatter.xml.HtmlCodeStyleSettings;
import com.intellij.ui.EnumComboBoxModel;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.ListItemsDialogWrapper;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class CodeStyleHtmlPanel extends CodeStyleAbstractPanel {
  
  private JTextField myKeepBlankLines;
  private JComboBox myWrapAttributes;
  private JCheckBox myAlignAttributes;
  private JCheckBox myKeepWhiteSpaces;

  private JPanel myPanel;
  private JPanel myPreviewPanel;

  private JCheckBox mySpacesAroundEquality;
  private JCheckBox mySpacesAroundTagName;
  private JCheckBox myAlignText;
  private TextFieldWithBrowseButton myInsertNewLineTagNames;
  private TextFieldWithBrowseButton myRemoveNewLineTagNames;
  private TextFieldWithBrowseButton myDoNotAlignChildrenTagNames;
  private TextFieldWithBrowseButton myKeepWhiteSpacesTagNames;
  private TextFieldWithBrowseButton myInlineElementsTagNames;
  private JTextField myDoNotAlignChildrenMinSize;
  private JCheckBox myShouldKeepBlankLines;
  private JCheckBox mySpaceInEmptyTag;
  private JCheckBox myWrapText;
  private JCheckBox myShouldKeepLineBreaksInText;
  private TextFieldWithBrowseButton myDontBreakIfInlineContent;
  private JBScrollPane myJBScrollPane;
  private JPanel myRightMarginPanel;
  private JComboBox myQuotesCombo;
  private JBCheckBox myEnforceQuotesBox;
  private ComboBox myBeforeFirstAttributeCombo;
  private ComboBox myAfterLastAttributeCombo;
  private JPanel mySettingsPanel;
  private RightMarginForm myRightMarginForm;
  private final List<HtmlCodeStylePanelExtension.HtmlPanelCustomizer> myPanelCustomizers;

  public CodeStyleHtmlPanel(CodeStyleSettings settings) {
    super(settings);
    installPreviewPanel(myPreviewPanel);

    fillWrappingCombo(myWrapAttributes);
    fillEnumCombobox(myQuotesCombo, CodeStyleSettings.QuoteStyle.class);
    fillEnumCombobox(myBeforeFirstAttributeCombo, CodeStyleSettings.HtmlTagNewLineStyle.class);
    fillEnumCombobox(myAfterLastAttributeCombo, CodeStyleSettings.HtmlTagNewLineStyle.class);

    customizeField(ApplicationBundle.message("title.insert.new.line.before.tags"), myInsertNewLineTagNames);
    customizeField(ApplicationBundle.message("title.remove.line.breaks.before.tags"), myRemoveNewLineTagNames);
    customizeField(ApplicationBundle.message("title.do.not.indent.children.of"), myDoNotAlignChildrenTagNames);
    customizeField(ApplicationBundle.message("title.inline.elements"), myInlineElementsTagNames);
    customizeField(ApplicationBundle.message("title.keep.whitespaces.inside"), myKeepWhiteSpacesTagNames);
    customizeField(ApplicationBundle.message("title.dont.wrap.if.inline.content"), myDontBreakIfInlineContent);

    myInsertNewLineTagNames.getTextField().setColumns(5);
    myRemoveNewLineTagNames.getTextField().setColumns(5);
    myDoNotAlignChildrenTagNames.getTextField().setColumns(5);
    myKeepWhiteSpacesTagNames.getTextField().setColumns(5);
    myInlineElementsTagNames.getTextField().setColumns(5);
    myDontBreakIfInlineContent.getTextField().setColumns(5);

    myQuotesCombo.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        boolean quotesRequired = !CodeStyleSettings.QuoteStyle.None.equals(myQuotesCombo.getSelectedItem());
        myEnforceQuotesBox.setEnabled(quotesRequired);
        if (!quotesRequired) myEnforceQuotesBox.setSelected(false);
      }
    });
    addPanelToWatch(myPanel);

    myPanelCustomizers = HtmlCodeStylePanelExtension.getCustomizers();
    myPanelCustomizers.forEach(customizer -> customizer.customizeSettingsPanel(mySettingsPanel));
  }

  @Override
  protected EditorHighlighter createHighlighter(final EditorColorsScheme scheme) {
    return XmlHighlighterFactory.createXMLHighlighter(scheme);
  }

  private void createUIComponents() {
    myRightMarginForm = new RightMarginForm(StdFileTypes.HTML.getLanguage(), getSettings());
    myRightMarginPanel = myRightMarginForm.getTopPanel();
    myJBScrollPane = new JBScrollPane() {
      @Override
      public Dimension getPreferredSize() {
        Dimension prefSize = super.getPreferredSize();
        return new Dimension(prefSize.width + 15, prefSize.height);
      }
    };
  }

  private static void customizeField(final String title, final TextFieldWithBrowseButton uiField) {
    ListItemsDialogWrapper.installListItemsDialogForTextField(uiField, () -> new TagListDialog(title));
  }

  @Override
  protected int getRightMargin() {
    return 60;
  }

  @Override
  public void apply(CodeStyleSettings rootSettings) throws ConfigurationException {
    HtmlCodeStyleSettings settings = rootSettings.getCustomSettings(HtmlCodeStyleSettings.class);
    settings.HTML_KEEP_BLANK_LINES = getIntValue(myKeepBlankLines);
    settings.HTML_ATTRIBUTE_WRAP = ourWrappings[myWrapAttributes.getSelectedIndex()];
    settings.HTML_TEXT_WRAP = myWrapText.isSelected() ? CommonCodeStyleSettings.WRAP_AS_NEEDED : CommonCodeStyleSettings.DO_NOT_WRAP;
    settings.HTML_SPACE_INSIDE_EMPTY_TAG = mySpaceInEmptyTag.isSelected();
    settings.HTML_ALIGN_ATTRIBUTES = myAlignAttributes.isSelected();
    settings.HTML_ALIGN_TEXT = myAlignText.isSelected();
    settings.HTML_KEEP_WHITESPACES = myKeepWhiteSpaces.isSelected();
    settings.HTML_SPACE_AROUND_EQUALITY_IN_ATTRIBUTE = mySpacesAroundEquality.isSelected();
    settings.HTML_SPACE_AFTER_TAG_NAME = mySpacesAroundTagName.isSelected();

    settings.HTML_ELEMENTS_TO_INSERT_NEW_LINE_BEFORE = myInsertNewLineTagNames.getText();
    settings.HTML_ELEMENTS_TO_REMOVE_NEW_LINE_BEFORE = myRemoveNewLineTagNames.getText();
    settings.HTML_DO_NOT_INDENT_CHILDREN_OF = myDoNotAlignChildrenTagNames.getText();
    settings.HTML_DO_NOT_ALIGN_CHILDREN_OF_MIN_LINES = getIntValue(myDoNotAlignChildrenMinSize);
    settings.HTML_INLINE_ELEMENTS = myInlineElementsTagNames.getText();
    settings.HTML_DONT_ADD_BREAKS_IF_INLINE_CONTENT = myDontBreakIfInlineContent.getText();
    settings.HTML_KEEP_WHITESPACES_INSIDE = myKeepWhiteSpacesTagNames.getText();
    settings.HTML_KEEP_LINE_BREAKS = myShouldKeepBlankLines.isSelected();
    settings.HTML_KEEP_LINE_BREAKS_IN_TEXT = myShouldKeepLineBreaksInText.isSelected();
    settings.HTML_QUOTE_STYLE = (CodeStyleSettings.QuoteStyle)myQuotesCombo.getSelectedItem();
    settings.HTML_ENFORCE_QUOTES = myEnforceQuotesBox.isSelected();
    settings.HTML_NEWLINE_BEFORE_FIRST_ATTRIBUTE = (CodeStyleSettings.HtmlTagNewLineStyle)myBeforeFirstAttributeCombo.getSelectedItem();
    settings.HTML_NEWLINE_AFTER_LAST_ATTRIBUTE = (CodeStyleSettings.HtmlTagNewLineStyle)myAfterLastAttributeCombo.getSelectedItem();
    myRightMarginForm.apply(rootSettings);

    myPanelCustomizers.forEach(el -> el.apply(rootSettings));
  }

  private static int getIntValue(JTextField keepBlankLines) {
    try {
      return Integer.parseInt(keepBlankLines.getText());
    }
    catch (NumberFormatException e) {
      return 0;
    }
  }

  @Override
  protected void resetImpl(final CodeStyleSettings rootSettings) {
    HtmlCodeStyleSettings settings = rootSettings.getCustomSettings(HtmlCodeStyleSettings.class);
    myKeepBlankLines.setText(String.valueOf(settings.HTML_KEEP_BLANK_LINES));
    myWrapAttributes.setSelectedIndex(getIndexForWrapping(settings.HTML_ATTRIBUTE_WRAP));
    myWrapText.setSelected(settings.HTML_TEXT_WRAP != CommonCodeStyleSettings.DO_NOT_WRAP);
    mySpaceInEmptyTag.setSelected(settings.HTML_SPACE_INSIDE_EMPTY_TAG);
    myAlignAttributes.setSelected(settings.HTML_ALIGN_ATTRIBUTES);
    myAlignText.setSelected(settings.HTML_ALIGN_TEXT);
    myKeepWhiteSpaces.setSelected(settings.HTML_KEEP_WHITESPACES);
    mySpacesAroundTagName.setSelected(settings.HTML_SPACE_AFTER_TAG_NAME);
    mySpacesAroundEquality.setSelected(settings.HTML_SPACE_AROUND_EQUALITY_IN_ATTRIBUTE);
    myShouldKeepBlankLines.setSelected(settings.HTML_KEEP_LINE_BREAKS);
    myShouldKeepLineBreaksInText.setSelected(settings.HTML_KEEP_LINE_BREAKS_IN_TEXT);

    myInsertNewLineTagNames.setText(settings.HTML_ELEMENTS_TO_INSERT_NEW_LINE_BEFORE);
    myRemoveNewLineTagNames.setText(settings.HTML_ELEMENTS_TO_REMOVE_NEW_LINE_BEFORE);
    myDoNotAlignChildrenTagNames.setText(settings.HTML_DO_NOT_INDENT_CHILDREN_OF);
    myDoNotAlignChildrenMinSize.setText(settings.HTML_DO_NOT_ALIGN_CHILDREN_OF_MIN_LINES == 0 ? "" : String.valueOf(settings.HTML_DO_NOT_ALIGN_CHILDREN_OF_MIN_LINES));
    myInlineElementsTagNames.setText(settings.HTML_INLINE_ELEMENTS);
    myDontBreakIfInlineContent.setText(settings.HTML_DONT_ADD_BREAKS_IF_INLINE_CONTENT);
    myKeepWhiteSpacesTagNames.setText(settings.HTML_KEEP_WHITESPACES_INSIDE);
    myRightMarginForm.reset(rootSettings);
    myQuotesCombo.setSelectedItem(settings.HTML_QUOTE_STYLE);
    myEnforceQuotesBox.setSelected(settings.HTML_ENFORCE_QUOTES);
    myBeforeFirstAttributeCombo.setSelectedItem(settings.HTML_NEWLINE_BEFORE_FIRST_ATTRIBUTE);
    myAfterLastAttributeCombo.setSelectedItem(settings.HTML_NEWLINE_AFTER_LAST_ATTRIBUTE);

    myPanelCustomizers.forEach(el -> el.reset(rootSettings));
  }

  @Override
  public boolean isModified(CodeStyleSettings rootSettings) {
    HtmlCodeStyleSettings settings = rootSettings.getCustomSettings(HtmlCodeStyleSettings.class);
    if (settings.HTML_KEEP_BLANK_LINES != getIntValue(myKeepBlankLines)) {
      return true;
    }
    if (settings.HTML_ATTRIBUTE_WRAP != ourWrappings[myWrapAttributes.getSelectedIndex()]) {
      return true;
    }

    if ((settings.HTML_TEXT_WRAP == CommonCodeStyleSettings.WRAP_AS_NEEDED) != myWrapText.isSelected()) {
      return true;
    }

    if (settings.HTML_SPACE_INSIDE_EMPTY_TAG != mySpaceInEmptyTag.isSelected()) {
      return true;
    }

    if (settings.HTML_ALIGN_ATTRIBUTES != myAlignAttributes.isSelected()) {
      return true;
    }

    if (settings.HTML_ALIGN_TEXT != myAlignText.isSelected()) {
      return true;
    }

    if (settings.HTML_KEEP_WHITESPACES != myKeepWhiteSpaces.isSelected()) {
      return true;
    }

    if (settings.HTML_SPACE_AROUND_EQUALITY_IN_ATTRIBUTE != mySpacesAroundEquality.isSelected()) {
      return true;
    }

    if (settings.HTML_SPACE_AFTER_TAG_NAME != mySpacesAroundTagName.isSelected()) {
      return true;
    }

    if (!Comparing.equal(settings.HTML_ELEMENTS_TO_INSERT_NEW_LINE_BEFORE, myInsertNewLineTagNames.getText().trim())) {
      return true;
    }

    if (!Comparing.equal(settings.HTML_ELEMENTS_TO_REMOVE_NEW_LINE_BEFORE, myRemoveNewLineTagNames.getText().trim())) {
      return true;
    }

    if (!Comparing.equal(settings.HTML_DO_NOT_INDENT_CHILDREN_OF, myDoNotAlignChildrenTagNames.getText().trim())) {
      return true;
    }

    if (settings.HTML_DO_NOT_ALIGN_CHILDREN_OF_MIN_LINES != getIntValue(myDoNotAlignChildrenMinSize)) {
      return true;
    }

    if (!Comparing.equal(settings.HTML_INLINE_ELEMENTS, myInlineElementsTagNames.getText().trim())) return true;
    if (!Comparing.equal(settings.HTML_DONT_ADD_BREAKS_IF_INLINE_CONTENT, myDontBreakIfInlineContent.getText().trim())) return true;

    if (!Comparing.equal(settings.HTML_KEEP_WHITESPACES_INSIDE, myKeepWhiteSpacesTagNames.getText().trim())) {
      return true;
    }

    if (myShouldKeepBlankLines.isSelected() != settings.HTML_KEEP_LINE_BREAKS) {
      return true;
    }

    if (myShouldKeepLineBreaksInText.isSelected() != settings.HTML_KEEP_LINE_BREAKS_IN_TEXT) {
      return true;
    }

    if (myQuotesCombo.getSelectedItem() != settings.HTML_QUOTE_STYLE) {
      return true;
    }
    if (myBeforeFirstAttributeCombo.getSelectedItem() != settings.HTML_NEWLINE_BEFORE_FIRST_ATTRIBUTE) {
      return true;
    }
    if (myAfterLastAttributeCombo.getSelectedItem() != settings.HTML_NEWLINE_AFTER_LAST_ATTRIBUTE) {
      return true;
    }

    if (myRightMarginForm.isModified(rootSettings) || myEnforceQuotesBox.isSelected() != settings.HTML_ENFORCE_QUOTES) return true;
    
    return myPanelCustomizers.stream().anyMatch(el -> el.isModified(rootSettings));
  }

  @Override
  public JComponent getPanel() {
    return myPanel;
  }

  @Override
  protected String getPreviewText() {
    return readFromFile(this.getClass(), "preview.html.template");

  }

  @Override
  @NotNull
  protected FileType getFileType() {
    return StdFileTypes.HTML;
  }

  private static <T extends Enum<T>> void fillEnumCombobox(JComboBox combo, Class<T> enumClass) {
    //noinspection unchecked
    combo.setModel(new EnumComboBoxModel<>(enumClass));
  }
}
