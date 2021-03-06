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
package org.eclipse.che.plugin.embedjsexample.ide.view;

import com.google.common.base.Optional;
import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.ScriptInjector;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.texteditor.ContentInitializedHandler;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.ide.api.parts.base.BasePresenter;
import org.eclipse.che.ide.api.resources.*;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.plugin.embedjsexample.ide.HelloWorldResources;
import org.eclipse.che.plugin.embedjsexample.ide.common.Constants;
import org.vectomatic.dom.svg.ui.SVGResource;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Strings.isNullOrEmpty;
import static org.eclipse.che.ide.api.resources.Resource.FILE;


/**
 * @author Mathias Schaefer <mathias.schaefer@eclipsesource.com>
 */
@Singleton
public class HelloWorldViewPresenter extends BasePresenter implements HelloWorldView.ActionDelegate {

    private final HelloWorldView helloWorldView;

    private final String url = "http://localhost:9090/api/index.jsp";

    private final EditorAgent editorAgent;
    private String queryContent;

    public void show() {
        String query = getEditorText();
        fetchFromServer(url, query);
    }

    private void fetchFromServer(String url, String req) {
        RequestBuilder builder = new RequestBuilder(RequestBuilder.POST, url);
        try{
            builder.setHeader("Content-Type", "application/x-www-form-urlencoded");
            String query = URL.encodeQueryString("query") + "=" + URL.encodeQueryString(req);
            String content = URL.encodeQueryString("content") + "=" + URL.encodeQueryString(queryContent);
            String postData = query+"&"+content;
            builder.sendRequest(postData, new RequestCallback() {
                @Override
                public void onResponseReceived(Request request, Response response) {
                    String apiReco;
                    if(response.getStatusCode() == 200) {
                        apiReco = response.getText();
                    } else {
                        apiReco = response.getStatusText();
                    }
                    sayHello(apiReco);
                }

                @Override
                public void onError(Request request, Throwable throwable) {
                    sayHello("Error: Can't fatch data from server!");
                }
            });
        } catch (Exception e) {
            Log.info(HelloWorldViewPresenter.class,  e.getStackTrace());
        }
    }

    private String getEditorText() {
        TextEditor textEditor = (TextEditor) editorAgent.getActiveEditor();

        if(textEditor != null) {
            String content = textEditor.getEditorWidget().getValue();
            int index = content.indexOf("??");
            String newValue;
            String query;
            if(index > -1) {
                String tmp = content.substring(index);
                newValue = content.substring(0, index) + tmp.substring(tmp.indexOf("\n"));
                query = content.substring(index+2, content.indexOf("\n", index));
                if(content.indexOf("\n", index) == -1) {
                    query = content.substring(index+2);
                }
                queryContent = content.substring(0, index);
            }
            else {
                newValue = content;
                queryContent = content;
                query = "";
            }
            textEditor.getEditorWidget().setValue(newValue, new MyContentInitializedHandler(textEditor));

//            List<HotKeyItem> hotKeyItemList = textEditor.getEditorWidget().getHotKeys();
//            for(HotKeyItem item: hotKeyItemList) {
//                query += item.getHotKey() + "\n";
//                query += item.getActionDescription() + "\n";
//            }
            return query;
        } else {
            return "Error: Can't get editor text!";
        }
    }

    @Inject
    public HelloWorldViewPresenter(final HelloWorldView helloWorldView, EditorAgent editorAgent) {
        this.helloWorldView = helloWorldView;
        this.editorAgent = editorAgent;
        ScriptInjector.fromUrl(GWT.getModuleBaseURL() + Constants.JAVASCRIPT_FILE_ID)
                .setWindow(ScriptInjector.TOP_WINDOW)
                .setCallback(new Callback<Void, Exception>() {
                    @Override
                    public void onSuccess(final Void result) {
                        Log.info(HelloWorldViewPresenter.class, Constants.JAVASCRIPT_FILE_ID + " loaded.");
                    }

                    @Override
                    public void onFailure(final Exception e) {
                        Log.error(HelloWorldViewPresenter.class, "Unable to load "+Constants.JAVASCRIPT_FILE_ID, e);
                    }
                }).inject();

    }

    public void sayHello(String content) {
        this.helloWorldView.sayHello(content);
    }

    @Override
    public String getTitle() {
        return "Code Show";
    }

    @Override
    public SVGResource getTitleImage() {
        return (HelloWorldResources.INSTANCE.icon());
    }

    @Override
    public View getView() {
        return helloWorldView;
    }

    @Override
    public String getTitleToolTip() {
        return getTitle();
    }

    @Override
    public void go(AcceptsOneWidget container) {
        container.setWidget(helloWorldView);
    }


    private class MyContentInitializedHandler implements ContentInitializedHandler {
        public TextEditor editor;

        public MyContentInitializedHandler(TextEditor editor) {
            this.editor = editor;
        }
        public void onContentInitialized() {
            editor.doSave();
        }
    }
}
