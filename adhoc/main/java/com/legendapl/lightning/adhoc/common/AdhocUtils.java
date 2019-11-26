package com.legendapl.lightning.adhoc.common;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

import com.legendapl.lightning.adhoc.adhocView.model.Adhoc;
import com.legendapl.lightning.adhoc.custom.CTCustomColumn;
import com.legendapl.lightning.adhoc.model.BaseModel;
import com.legendapl.lightning.adhoc.model.Domain;
import com.legendapl.lightning.adhoc.model.Field;
import com.legendapl.lightning.adhoc.model.Topic;
import com.sun.javafx.scene.text.TextLayout;
import com.sun.javafx.tk.Toolkit;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeItem;
import javafx.scene.text.Font;

/**
 * 共通メソッド
 *
 * @author Legend Applications China, LaKeel BI development team.
 * @since 2018/1/31
 */
@SuppressWarnings("restriction")
public class AdhocUtils {

	/*-------------------------------bundleMessage-------------------------------*/

	public static SimpleBooleanProperty isJapanese = new SimpleBooleanProperty();

	static {
		isJapanese.set(true);
	}

	public static ResourceBundle bundleMessage = ResourceBundle.getBundle("AdhocBundleMessage");
	
	public static Logger logger = Logger.getLogger(AdhocUtils.class);

	public static String getString(String key) {
		return bundleMessage.getString(key);
	}

	public static String getString(String key, Object... args) {
		return MessageFormat.format(getString(key), args);
	}

	public static String format(String s, Object... args) {
		return MessageFormat.format(s, args);
	}

	public static void changeLanguage(String language) {
		if (language.equals("日本語") || language.startsWith("ja")) { // Japanese
			bundleMessage = ResourceBundle.getBundle("AdhocBundleMessage_ja");
			isJapanese.set(true);
		} else { // English
			bundleMessage = ResourceBundle.getBundle("AdhocBundleMessage_en");
			isJapanese.set(false);
		}
	}

	/*-------------------------------layout-------------------------------*/

	@SuppressWarnings("serial")
	private static final Map<ModelType, Class<? extends BaseModel>> modelType2ClassType = Collections.unmodifiableMap(

			new HashMap<ModelType, Class<? extends BaseModel>>() {
				{
					put(ModelType.ADHOC, Adhoc.class);
					put(ModelType.TOPIC, Topic.class);
					put(ModelType.DOMAIN, Domain.class);
				}
			}
	);

	public static Class<? extends BaseModel> getClassType(ModelType modelType) {
		return modelType2ClassType.get(modelType);
	}

	public static final TextLayout layout = Toolkit.getToolkit().getTextLayoutFactory().createLayout();

	public static double computeTextWidth(Font font, String text, double wrappingWidth) {
        layout.setContent(text != null ? text : "", font.impl_getNativeFont());
        layout.setWrapWidth((float)wrappingWidth);
        return layout.getBounds().getWidth() * 1.1 + 10;
    }

	public static void autoWidth(TableView<?> table) {
    	Queue<TableColumn<?, ?>> queue = new LinkedList<TableColumn<?, ?>>();
		queue.addAll(table.getColumns());
		while (!queue.isEmpty()) {
			CTCustomColumn<?, ?> column = (CTCustomColumn<?, ?>) queue.poll();
			if (!column.getColumns().isEmpty()) {
				queue.addAll(column.getColumns());
			}
			double textwidth = computeTextWidth(new Font("Meiryo Bold", 13), column.getText(), -1);
			textwidth += 30;
			if(column.getWidth() == 80) {
				column.setPrefWidth(81);
			}
			if (column.getWidth() < textwidth) {
				column.addWidth(textwidth - column.getWidth());
			}
		}
    }

    public static double getLabelWidth(Font font, String text) {
    	return computeTextWidth(font, text, -1);
    }

    public static double getLabelWidth(Label label) {
    	return computeTextWidth(new Font("Meiryo Bold", 12), label.getText(), -1);
    }

    public static double getLabelWidth(String text) {
    	return computeTextWidth(new Font("Meiryo Bold", 12), text, -1);
    }

	public static double getChoiceBoxWidth(ChoiceBox<OperationType> choiceBox) {
		double maxWidth = choiceBox.getWidth();
		for(OperationType item: choiceBox.getItems()) {
			maxWidth = Math.max(maxWidth, getLabelWidth(item.toString() + 10));
		}
		return maxWidth;
	}

	public static double getMaxWidth(List<String> list) {
		double maxWidth = 0;
		for(String item: list) {
			double itemWidth = getLabelWidth(item);
			maxWidth = maxWidth > itemWidth ? maxWidth : itemWidth;
		}
		return maxWidth;
	}

	/*-------------------------------static-------------------------------*/

	public static <T> List<T> createNewListRemoveNull(List<T> list) {
		List<T> newList = new ArrayList<T>();
		if (null != list) {
			for (T t : list) {
				if (null != t) {
					newList.add(t);
				}
			}
		}
		return newList;
	}

	@SafeVarargs
	public static <T> List<T> createNewListRemoveNull(List<T>... lists) {
		List<T> newList = new ArrayList<T>();
		for (List<T> list : lists) {
			newList.addAll(createNewListRemoveNull(list));
		}
		return newList;
	}

	public static <T> TreeItem<T> createNewTreeItem(TreeItem<T> treeItem) {
		TreeItem<T> newTreeItem = new TreeItem<T>();
		if (null != treeItem) {
			newTreeItem.setValue(treeItem.getValue());
			newTreeItem.setGraphic(treeItem.getGraphic());
			newTreeItem.setExpanded(treeItem.isExpanded());
			for (TreeItem<T> child : treeItem.getChildren()) {
				TreeItem<T> newChild = createNewTreeItem(child);
				newTreeItem.getChildren().add(newChild);
			}
		}
		return newTreeItem;
	}

	public static <T> List<T> moveListElement(List<T> list, T element, int dir) {
		if (null != list) {
			int pos = list.indexOf(element);
			if (pos >= 0 && pos < list.size()) {
				if (pos + dir >= 0 && pos + dir < list.size()) {
					list.remove(pos);
					list.add(pos + dir, element);
				}
			}
		}
		return list;
	}

	public static <T> List<T> replaceListElement(List<T> list, T oldElement, T newElement) {
		if (null != list) {
			int pos = list.indexOf(oldElement);
			if (pos >= 0 && pos < list.size()) {
				list.remove(pos);
				list.add(pos, newElement);
			}
		}
		return list;
	}

	public static <T> void switchList(List<T> list1, List<T> list2) {
		if (null != list1 && null != list2) {
			List<T> newList1 = new ArrayList<T>();
			newList1.addAll(list1);
			List<T> newList2 = new ArrayList<T>();
			newList2.addAll(list2);
			list1.clear();
			list1.addAll(newList2);
			list2.clear();
			list2.addAll(newList1);
		}
	}

	public static <T> void setAll(List<T> list1, List<T> list2) {
		if (null != list1 && null != list2) {
			list1.clear();
			list1.addAll(list2);
		}
	}

	public static <T> List<TreeItem<T>> getAllItems(TreeItem<T> root) {
		List<TreeItem<T>> list = new ArrayList<>();
		if (null != root) {
			list.add(root);
			for (TreeItem<T> child : root.getChildren()) {
				list.addAll(getAllItems(child));
			}
		}
		return list;
	}
	
	public static <T> List<TreeItem<T>> getAllLeaves(TreeItem<T> root) {
		List<TreeItem<T>> leaves = new ArrayList<>();
		getAllItems(root).forEach(item -> {
			if (item.getChildren().size() == 0) leaves.add(item);
		});
		return leaves;
	}

	public static List<Field> getFieldsByRoot(Adhoc adhoc) {
		List<Field> fields = new ArrayList<>();
		if (null != adhoc && null != adhoc.getTopicTree()) {
			adhoc.getTopicTree().getAllItems().forEach(item -> {
				Field field = adhoc.getTopicTree().getFieldByResId(item.getResourceId());
				if (null != field) {
					fields.add(field);
				}
			});
		}
		return fields;
	}

	public static <T> T getFirstObject(List<T> list, Class<?> Class) {
		if (null != list) {
			for (int i = 0; i < list.size(); i ++) {
				T t = list.get(i);
				if (null != t && Class.equals(t.getClass())) {
					return t;
				}
			}
		}
		return null;
	}

	public static <T> T getFirstObjectInstance(List<T> list, Class<?> Class) {
		if (null != list) {
			for (int i = 0; i < list.size(); i ++) {
				T t = list.get(i);
				if (null != t && Class.isInstance(t)) {
					return t;
				}
			}
		}
		return null;
	}

	public static <T> T getLastObject(List<T> list, Class<?> Class) {
		if (null != list) {
			for (int i = list.size() - 1; i >= 0; i --) {
				T t = list.get(i);
				if (null != t && Class.equals(t.getClass())) {
					return t;
				}
			}
		}
		return null;
	}

	public static <T> T getLastObjectInstance(List<T> list, Class<?> Class) {
		if (null != list) {
			for (int i = list.size() - 1; i >= 0; i --) {
				T t = list.get(i);
				if (null != t && Class.isInstance(t)) {
					return t;
				}
			}
		}
		return null;
	}

	public static <T> Integer getFirstObjectIndex(List<T> list, Class<?> Class) {
		if (null != list) {
			for (int i = 0; i < list.size(); i ++) {
				T t = list.get(i);
				if (null != t && Class.equals(t.getClass())) {
					return i;
				}
			}
		}
		return -1;
	}

	public static <T> Integer getFirstObjectIndexInstance(List<T> list, Class<?> Class) {
		if (null != list) {
			for (int i = 0; i < list.size(); i ++) {
				T t = list.get(i);
				if (null != t && Class.isInstance(t)) {
					return i;
				}
			}
		}
		return -1;
	}

	public static <T> Integer getLastObjectIndex(List<T> list, Class<?> Class) {
		if (null != list) {
			for (int i = list.size() - 1; i >= 0; i --) {
				T t = list.get(i);
				if (null != t && Class.equals(t.getClass())) {
					return i;
				}
			}
		}
		return list.size();
	}

	public static <T> Integer getLastObjectIndexInstance(List<T> list, Class<?> Class) {
		if (null != list) {
			for (int i = list.size() - 1; i >= 0; i --) {
				T t = list.get(i);
				if (null != t && Class.isInstance(t)) {
					return i;
				}
			}
		}
		return list.size();
	}

	public static <T> List<T> getAllTargetChild(List<T> list, Class<?> Class) {
		List<T> getList = new ArrayList<>();
		if (null != list) {
			for (T t : list) {
				if (null != t && Class.equals(t.getClass())) {
					getList.add(t);
				}
			}
		}
		return getList;
	}

	public static <T> List<T> getAllTargetChildInstance(List<T> list, Class<?> Class) {
		List<T> getList = new ArrayList<>();
		if (null != list) {
			for (T t : list) {
				if (null != t && Class.isInstance(t)) {
					getList.add(t);
				}
			}
		}
		return getList;
	}

	public static <T> List<T> removeAllTargetChild(List<T> list, Class<?> Class) {
		if (null != list) {
			List<T> removeList = new ArrayList<>();
			for (T t : list) {
				if (null != t && Class.equals(t.getClass())) {
					removeList.add(t);
				}
			}
			list.removeAll(removeList);
		}
		return list;
	}

	public static <T> List<T> removeAllTargetChildInstance(List<T> list, Class<?> Class) {
		if (null != list) {
			List<T> removeList = new ArrayList<>();
			for (T t : list) {
				if (null != t && Class.isInstance(t)) {
					removeList.add(t);
				}
			}
			list.removeAll(removeList);
		}
		return list;
	}

	public static boolean listEleEq(List<Object> listA, List<Object> listB) {
		for(int i=listA.size()-1;i>=0;i--) {
			if(!listA.get(i).equals(listB.get(i))) {
				return false;
			}
		}
		return true;
	}

	public static String toString(Object ob) {
		return ob == null ? "" : ob.toString();
	}

}
