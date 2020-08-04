package com.company.platform.web.screens;

import com.haulmont.cuba.gui.components.BoxLayout;
import com.haulmont.cuba.gui.screen.Screen;
import com.haulmont.cuba.gui.screen.UiController;
import com.haulmont.cuba.gui.screen.UiDescriptor;

import javax.inject.Inject;
import javax.swing.*;

@UiController("platform_EmptyScreen")
@UiDescriptor("empty-screen.xml")
public class EmptyScreen extends Screen {
    /**
     * Layout in the page
     */
    @Inject
    private BoxLayout pageLayout;

    public BoxLayout getPageLayout(){return this.pageLayout;}
}