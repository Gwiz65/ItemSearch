/*
 * This is free and unencumbered software released into the public domain.
 *
 * Anyone is free to copy, modify, publish, use, compile, sell, or
 * distribute this software, either in source code form or as a compiled
 * binary, for any purpose, commercial or non-commercial, and by any
 * means.
 * 
 * In jurisdictions that recognize copyright laws, the author or authors
 * of this software dedicate any and all copyright interest in the
 * software to the public domain. We make this dedication for the benefit
 * of the public at large and to the detriment of our heirs and
 * successors. We intend this dedication to be an overt act of
 * relinquishment in perpetuity of all present and future rights to this
 * software under copyright law.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 * 
 * For more information, please refer to <http://unlicense.org/>
*/

package org.gwiz.wurmunlimited.mods.itemsearch;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Properties;

import org.gotti.wurmunlimited.modloader.classhooks.HookException;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;
import org.gotti.wurmunlimited.modloader.interfaces.Configurable;
import org.gotti.wurmunlimited.modloader.interfaces.Initable;
import org.gotti.wurmunlimited.modloader.interfaces.Versioned;
import org.gotti.wurmunlimited.modloader.interfaces.WurmClientMod;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtNewMethod;
import javassist.CtPrimitiveType;
import javassist.NotFoundException;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;

public class ItemSearch implements WurmClientMod, Initable, Versioned, Configurable {

	private static final String version = "0.44";
	private boolean addtoInventoryWindow = true;
	private boolean addtoInventoryContainerWindows = true;
	private boolean addtoItemListWindows = true;
	private boolean addtoSkillWindow = true;
	private boolean addtoSpellEffectWindow = true;
	private boolean addtoMissionWindow = true;
	private boolean addtoAchievementWindow = true;
	private boolean addtoStatisticsWindow = true;
	private boolean addtoDebugWindow = true;
	private boolean addtoJournalWindow = true;

	@Override
	public void configure(Properties properties) {
		addtoInventoryWindow = Boolean
				.parseBoolean(properties.getProperty("addtoInventoryWindow", Boolean.toString(addtoInventoryWindow)));
		addtoInventoryContainerWindows = Boolean.parseBoolean(properties.getProperty("addtoInventoryContainerWindows",
				Boolean.toString(addtoInventoryContainerWindows)));
		addtoItemListWindows = Boolean
				.parseBoolean(properties.getProperty("addtoItemListWindows", Boolean.toString(addtoItemListWindows)));
		addtoSkillWindow = Boolean
				.parseBoolean(properties.getProperty("addtoSkillWindow", Boolean.toString(addtoSkillWindow)));
		addtoSpellEffectWindow = Boolean.parseBoolean(
				properties.getProperty("addtoSpellEffectWindow", Boolean.toString(addtoSpellEffectWindow)));
		addtoMissionWindow = Boolean
				.parseBoolean(properties.getProperty("addtoMissionWindow", Boolean.toString(addtoMissionWindow)));
		addtoAchievementWindow = Boolean.parseBoolean(
				properties.getProperty("addtoAchievementWindow", Boolean.toString(addtoAchievementWindow)));
		addtoStatisticsWindow = Boolean
				.parseBoolean(properties.getProperty("addtoStatisticsWindow", Boolean.toString(addtoStatisticsWindow)));
		addtoDebugWindow = Boolean
				.parseBoolean(properties.getProperty("addtoDebugWindow", Boolean.toString(addtoDebugWindow)));
		addtoJournalWindow = Boolean
				.parseBoolean(properties.getProperty("addtoJournalWindow", Boolean.toString(addtoJournalWindow)));
	}

	@Override
	public void init() {
		try {
			ClassPool hookClassPool = HookManager.getInstance().getClassPool();

			// the class we are adding the search bar to
			CtClass ctWurmTreeList = hookClassPool.getCtClass("com.wurmonline.client.renderer.gui.WurmTreeList");

			// add input field listener interface
			ctWurmTreeList.addInterface(hookClassPool.getCtClass("com.wurmonline.client.renderer.gui.InputFieldListener"));

			// add some fields
			ctWurmTreeList.addField(new CtField(CtPrimitiveType.booleanType, "hasSearch", ctWurmTreeList), "false");
			ctWurmTreeList.addField(new CtField(CtPrimitiveType.booleanType, "matchFound", ctWurmTreeList), "false");
			ctWurmTreeList.addField(new CtField(hookClassPool.getCtClass("com.wurmonline.client.renderer.gui.WurmInputField"),
							"searchField", ctWurmTreeList));
			ctWurmTreeList.addField(new CtField(hookClassPool.getCtClass("com.wurmonline.client.renderer.gui.WButton"),
					"searchButton", ctWurmTreeList));
			ctWurmTreeList.addField(new CtField(hookClassPool.getCtClass("com.wurmonline.client.renderer.gui.WButton"),
					"clearButton", ctWurmTreeList));

			// build an if statement
			boolean hasOne = false;
			StringBuilder ifStatement = new StringBuilder("if (");
			if (addtoInventoryWindow) {
				ifStatement.append("callingClassName.contains(\"InventoryWindow\")");
				hasOne = true;
			}
			if (addtoInventoryContainerWindows) {
				if (hasOne)
					ifStatement.append(" || ");
				ifStatement.append("callingClassName.contains(\"InventoryContainerWindow\")");
				hasOne = true;
			}
			if (addtoItemListWindows) {
				if (hasOne)
					ifStatement.append(" || ");
				ifStatement.append("callingClassName.contains(\"ItemListWindow\")");
				hasOne = true;
			}
			if (addtoSkillWindow) {
				if (hasOne)
					ifStatement.append(" || ");
				ifStatement.append("callingClassName.contains(\"SkillWindowComponent\")");
				hasOne = true;
			}
			if (addtoSpellEffectWindow) {
				if (hasOne)
					ifStatement.append(" || ");
				ifStatement.append("callingClassName.contains(\"SpellEffectWindow\")");
				hasOne = true;
			}
			if (addtoMissionWindow) {
				if (hasOne)
					ifStatement.append(" || ");
				ifStatement.append("callingClassName.contains(\"MissionWindow\")");
				hasOne = true;
			}
			if (addtoAchievementWindow) {
				if (hasOne)
					ifStatement.append(" || ");
				ifStatement.append("callingClassName.contains(\"AchievementWindow\")");
				hasOne = true;
			}
			if (addtoStatisticsWindow) {
				if (hasOne)
					ifStatement.append(" || ");
				ifStatement.append("callingClassName.contains(\"StatisticsWindow\")");
				hasOne = true;
			}
			if (addtoDebugWindow) {
				if (hasOne)
					ifStatement.append(" || ");
				ifStatement.append("callingClassName.contains(\"DebugWindowComponent\")");
				hasOne = true;
			}
			if (addtoJournalWindow) {
				if (hasOne)
					ifStatement.append(" || ");
				ifStatement.append("callingClassName.contains(\"JournalWindow\")");
			}
			// not sure why anyone would set search off for all windows, but just in case
			if (ifStatement.toString().equals("if ("))
				ifStatement.append("false) {");
			else
				ifStatement.append(") {");
			final String insertString = ifStatement.toString();

			// create the search bar components
			CtConstructor ctWurmTreeListConstructor = ctWurmTreeList.getDeclaredConstructor(new CtClass[] {
					hookClassPool.getCtClass("java.lang.String"), hookClassPool.getCtClass("[I"),
					hookClassPool.getCtClass("java.lang.String[]"), hookClassPool.getCtClass("java.lang.String[]"),
					CtPrimitiveType.booleanType, hookClassPool.getCtClass("java.lang.String[]"),
					CtPrimitiveType.booleanType, CtPrimitiveType.booleanType,
					hookClassPool.getCtClass("com.wurmonline.client.renderer.gui.TreeListItem"),
					CtPrimitiveType.booleanType, CtPrimitiveType.booleanType, CtPrimitiveType.booleanType });
			ctWurmTreeListConstructor.instrument(new ExprEditor() {
				public void edit(MethodCall methodCall) throws CannotCompileException {
					if (methodCall.getMethodName().equals("setComponent")) {
						methodCall.replace("{ if ($0.equals(this)) { StackTraceElement[] stes = Thread.currentThread()."
								+ "getStackTrace(); for (int i = 2; i < stes.length; i++) if (stes[i]."
								+ "getClassName().contains(\"Window\")) { String callingClassName = stes[i].getClassName();"
								+ insertString + "this.hasSearch = true; this.searchField = new com.wurmonline.client."
								+ "renderer.gui.WurmInputField(\"Search\", this); final com.wurmonline.client."
								+ "renderer.gui.WurmArrayPanel searchArray = new com.wurmonline.client.renderer."
								+ "gui.WurmArrayPanel(1); this.searchButton = new com.wurmonline.client.renderer."
								+ "gui.WButton(\"Search\", this); this.clearButton = new com.wurmonline.client."
								+ "renderer.gui.WButton(\"Clear\", this); searchArray.addComponent(this.searchField);"
								+ " searchArray.addComponent(this.searchButton); searchArray.addComponent("
								+ "this.clearButton); final com.wurmonline.client.renderer.gui.WurmBorderPanel "
								+ "mainPanel = new com.wurmonline.client.renderer.gui.WurmBorderPanel(); "
								+ "mainPanel.setComponent($1, 4); mainPanel.setComponent(searchArray, 2); "
								+ "this.setComponent(mainPanel, 4); this.searchField.setInitialSize(150, "
								+ "this.searchButton.height, false); } else $_ = $proceed($$); break; } } "
								+ "else $_ = $proceed($$); }");
					}
				}
			});

			// create an override for component resized to keep our stuff the right width
			ctWurmTreeList.addMethod(CtNewMethod.make("public void componentResized() { this.layout(); if "
					+ "(this.hasSearch) this.searchField.setSize(this.width - this.searchButton.width - "
					+ "this.clearButton.width, this.searchButton.height); }", ctWurmTreeList));

			// create override methods for InputFieldListener interface
			ctWurmTreeList.addMethod(CtNewMethod.make(
					"public void handleInput(String input) { this.searchField.setTextMoveToEnd(input); }",
					ctWurmTreeList));
			ctWurmTreeList.addMethod(CtNewMethod
					.make("public void handleInputChanged(com.wurmonline.client.renderer.gui.WurmInputField field, "
							+ "String input) { this.recalcLines(); }", ctWurmTreeList));
			ctWurmTreeList.addMethod(CtNewMethod.make(
					"public void handleEscape(com.wurmonline.client.renderer.gui.WurmInputField field) { }",
					ctWurmTreeList));

			// handle our buttons
			ctWurmTreeList.getDeclaredMethod("buttonClicked").insertAfter("if (this.hasSearch) { if (button.equals"
					+ "(this.searchButton)) this.recalcLines(); if (button.equals(this.clearButton)) { this.searchField"
					+ ".setText(\"\"); this.recalcLines(); } } ");

			// create a method to search children recursively
			ctWurmTreeList.addMethod(CtNewMethod.make("public void checkForMatch(com.wurmonline.client.renderer.gui."
					+ "WTreeListNode node) { String itemname = ((com.wurmonline.client.renderer.gui.TreeListItem) "
					+ "node.item).getName(); if (itemname == null) this.shouldRecalcLines = true; else { if (itemname."
					+ "toLowerCase().contains(this.searchField.getText().toLowerCase()) || itemname.equals(\"inventory\") "
					+ "|| itemname.equals(\"body\")) this.matchFound = true; } if (node.children.isEmpty()) return; for "
					+ "(int i = 0; i < node.children.size(); i++) this.checkForMatch((com.wurmonline.client.renderer.gui."
					+ "WTreeListNode) node.children.get(i)); }", ctWurmTreeList));

			// inject search filter into addLines method
			ctWurmTreeList.getDeclaredMethod("addLines").instrument(new ExprEditor() {
				public void edit(MethodCall methodCall) throws CannotCompileException {
					if (methodCall.getMethodName().equals("add")) {
						methodCall.replace("{ if (this.hasSearch) { if (!this.searchField.getText().equals(\"\")) { "
								+ "this.matchFound = false; this.checkForMatch(child); if (this.matchFound) $_ = "
								+ "$proceed($$); } else $_ = $proceed($$); } else $_ = $proceed($$); }");
					}
				}
			});

			// force container windows to preload
			hookClassPool.getCtClass("com.wurmonline.client.renderer.gui.InventoryListComponent")
					.getDeclaredConstructor(new CtClass[] {
							hookClassPool.getCtClass("com.wurmonline.client.game.inventory.InventoryMetaWindowView"),
							CtPrimitiveType.booleanType, CtPrimitiveType.booleanType, CtPrimitiveType.booleanType })
					.insertAfter("this.preload = true;");

		} catch (NotFoundException | CannotCompileException e) {
			appendToFile(e);
			throw new HookException(e);
		}
	}

	// For anyone modding the client, this is seriously useful. It will write in
	// exception.txt if the code can't be injected in the client before it launches.
	public static void appendToFile(Exception e) {
		try {
			FileWriter fstream = new FileWriter("exception.txt", true);
			BufferedWriter out = new BufferedWriter(fstream);
			PrintWriter pWriter = new PrintWriter(out, true);
			e.printStackTrace(pWriter);
		} catch (Exception ie) {
			throw new RuntimeException("Could not write Exception to file", ie);
		}
	}

	@Override
	public String getVersion() {
		return version;
	}
}
