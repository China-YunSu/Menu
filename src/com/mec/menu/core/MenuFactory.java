package com.mec.menu.core;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Method;

import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.mec.menu.anno.menuAnno;
import com.mec.util.XMLParse;

public abstract class MenuFactory {
	private JComponent menuContain;
	private MenuActionDefination menuActionPool;
	
	public MenuFactory(JComponent menuContain) {
		this();
		setMenuContain(menuContain);
	}
	
	public MenuFactory() {
		menuActionPool = new MenuActionDefination();
	}

	public void setMenuContain(JComponent menuContain) {
		this.menuContain = menuContain;
	}

	public void setActionObject(Object objcet) {
		menuActionPool.setObject(objcet);
	}
	
	private JMenu processMenu(JMenu menu, Element element) {
		JMenu oneMenu = new JMenu();
		dealMenuAttribute(oneMenu, element);
		if (menu == null) {
			menuContain.add(oneMenu);
		} else {
			menu.add(oneMenu);
		}
		return oneMenu;
	}

	private void dealMenuItem(JMenu menu, Element element) {
		JMenuItem item = new JMenuItem();
		dealItemAttribute(item, element);
		Method method = menuActionPool.getMethod(item.getText());
		if (method != null) {
			dealItemAction(item, method);
		}
		menu.add(item);
	}

	public void loadActionProcess(Class<?> action) {
		try {
			for (Method method : action.getMethods()) {
				if (method.isAnnotationPresent(menuAnno.class)) {
					menuAnno anno = method.getAnnotation(menuAnno.class);
					menuActionPool.addMethod(anno.item(), method);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void loadActionProcess(String className) {
		try {
			Class<?> klass = Class.forName(className);
			loadActionProcess(klass);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	private void dealItemAction(JMenuItem item, Method method) {
		item.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					method.invoke(menuActionPool.getObject());
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});
	}

	private void dealMenu(JMenu menu, Element element) {
		new XMLParse() {

			@Override
			public void dealElement(Element element, int index) {
				String name = element.getTagName();
				if (name.equalsIgnoreCase("menu")) {
					JMenu oneMenu = processMenu(menu, element);
					dealMenu(oneMenu, element);
				}

				if (name.equalsIgnoreCase("item")) {
					dealMenuItem(menu, element);
					return;
				}

				if (name.equalsIgnoreCase("separator")) {
					menu.addSeparator();
				}
			}
		}.parseElement(element);
	}

	public void loadActionMapping(String XMLPath) {
		Document document = XMLParse.getDocument(XMLPath);
		if (document == null) {
			System.out.println("文件路径[" + XMLPath +"]不存在");
			return;
		}
		new XMLParse() {
			
			@Override
			public void dealElement(Element element, int index) {
				try {
					String className = element.getAttribute("class");
					Class<?> klass = Class.forName(className);
					new XMLParse() {
						
						@Override
						public void dealElement(Element element, int index) {
							try {
							String item = element.getAttribute("item");
							String methodName = element.getAttribute("method");
							Method method = klass.getMethod(methodName);
							menuActionPool.addMethod(item, method);
							} catch (NoSuchMethodException e) {
								e.printStackTrace();
							} catch (SecurityException e) {
								e.printStackTrace();
							}
						}
					}.parseTagByElement(element, "Mapping");
					
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
		}.parseTagByDocument(document, "Mappings");
		
	}
	
	protected abstract void dealItemAttribute(JMenuItem item, Element element);

	protected abstract void dealMenuAttribute(JMenu oneMenu, Element element);

	public MenuFactory loadMenu(String XMLPath) {
		new XMLParse() {

			@Override
			public void dealElement(Element element, int index) {
				dealMenu(null, element);
			}
		}.parseRoot(XMLParse.getDocument(XMLPath));

		return this;
	}
}
