/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.embedjsexample.ide;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.DefaultActionGroup;
import org.eclipse.che.ide.api.action.IdeActions;
import org.eclipse.che.ide.api.constraints.Constraints;
import org.eclipse.che.ide.api.extension.Extension;
import org.eclipse.che.plugin.embedjsexample.ide.action.HelloWorldAction;

import static org.eclipse.che.ide.api.action.IdeActions.GROUP_HELP;
import static org.eclipse.che.ide.api.action.IdeActions.GROUP_MAIN_MENU;
import static org.eclipse.che.ide.api.constraints.Anchor.AFTER;

/**
 * @author Mathias Schaefer <mathias.schaefer@eclipsesource.com>
 */
@Extension(title = "Hello world from JavaScript example")
@Singleton
public class HelloWorldViewExampleExtension {

    @Inject
    public HelloWorldViewExampleExtension(ActionManager actionManager, HelloWorldAction codeShowAction) {
        actionManager.registerAction("myCodeShowAction", codeShowAction);
        DefaultActionGroup myGroup = new DefaultActionGroup("My Code Show Action", true, actionManager);

        myGroup.add(codeShowAction);

        DefaultActionGroup mainMenu = (DefaultActionGroup)actionManager.getAction(GROUP_MAIN_MENU);
        mainMenu.add(myGroup, new Constraints(AFTER, GROUP_HELP));

        DefaultActionGroup toolbar = (DefaultActionGroup)actionManager.getAction(IdeActions.GROUP_CENTER_TOOLBAR);
        toolbar.add(codeShowAction, Constraints.FIRST);

        DefaultActionGroup mainContextMenuGroup = (DefaultActionGroup)actionManager.getAction("resourceOperation");
        mainContextMenuGroup.add(codeShowAction, Constraints.LAST);
    }

}
