package com.neogenesis.pfaat.swingx;


import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.util.*;


/**
 * Manage a group of radio menu items.
 *
 * @author $Author: xih $
 * @version $Revision: 1.2 $, $Date: 2002/10/11 18:31:09 $ */
public class GroupManager {
    protected Map map = new TreeMap();
    protected String selection;
    protected String name;
    protected ActionListener action_listener;
    protected GroupManagerMenu menu;

    public GroupManager(String name, ActionListener action_listener) {
        this.name = name;
        this.action_listener = action_listener;
    }

    public JMenu getJMenu() {
        if (menu == null) {
            menu = new GroupManagerMenu();
            menu.setSelection();
        }
        return menu;
    }

    protected void addItem(String key, Object value) {
        map.put(key, value);
    }

    public String getSelectionKey() {
        return selection;
    }

    public void setSelectionKey(String selection) { 
        this.selection = selection;
        if (menu != null)
            menu.setSelection();
    }

    public Object getSelection() {
        return map.get(selection);
    }

    private class GroupManagerActionListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            JRadioButtonMenuItem source =
                (JRadioButtonMenuItem) event.getSource();

            if (source.isSelected()) {
                setSelectionKey(source.getText());
                action_listener.actionPerformed(event);
            }
        }
    }
    

    private class GroupManagerMenu extends JMenu {
        private ButtonGroup group = new ButtonGroup();
        private JRadioButtonMenuItem[] items;

        public GroupManagerMenu() {
            super(GroupManager.this.name);
            items = new JRadioButtonMenuItem[map.size()];
            int count = 0;
            GroupManagerActionListener group_action = 
                new GroupManagerActionListener();

            for (Iterator iterator = map.keySet().iterator();
                iterator.hasNext(); count++) {
                String key = (String) iterator.next();
                JRadioButtonMenuItem item = 
                    new JRadioButtonMenuItem(key, false);

                items[count] = item;
                item.addActionListener(group_action);
                add(item);
                group.add(item);
            }
            setSelection();
        }

        private void setSelection() {
            for (int i = 0; i < items.length; i++)
                if (items[i].getText().equals(selection)) {
                    items[i].setSelected(true);
                    return;
                }
        }
	
        public void stateChanged(ChangeEvent e) {
            setSelection();
        }
    }
    
}
