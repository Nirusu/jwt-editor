package burp;

import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.intruder.Intruder;
import burp.api.montoya.persistence.Preferences;
import burp.api.montoya.proxy.Proxy;
import burp.api.montoya.ui.UserInterface;
import burp.api.montoya.utilities.ByteUtils;
import burp.config.BurpConfig;
import burp.config.BurpConfigPersistence;
import burp.intruder.JWSPayloadProcessor;
import burp.proxy.ProxyConfig;
import burp.proxy.ProxyHttpMessageHandler;
import burp.proxy.ProxyWsMessageHandler;
import com.blackberry.jwteditor.model.keys.KeysModel;
import com.blackberry.jwteditor.model.persistence.BurpKeysModelPersistence;
import com.blackberry.jwteditor.model.persistence.KeysModelPersistence;
import com.blackberry.jwteditor.presenter.PresenterStore;
import com.blackberry.jwteditor.utils.Utils;
import com.blackberry.jwteditor.view.burp.BurpView;
import com.blackberry.jwteditor.view.editor.RequestEditorView;
import com.blackberry.jwteditor.view.editor.ResponseEditorView;
import com.blackberry.jwteditor.view.rsta.DefaultRstaFactory;
import com.blackberry.jwteditor.view.rsta.RstaFactory;

import java.awt.*;

import static burp.api.montoya.ui.editor.extension.EditorMode.READ_ONLY;

@SuppressWarnings("unused")
public class JWTEditorExtension implements BurpExtension {

    @Override
    public void initialize(MontoyaApi api) {
        api.extension().setName(Utils.getResourceString("tool_name"));

        Preferences preferences = api.persistence().preferences();
        KeysModelPersistence keysModelPersistence = new BurpKeysModelPersistence(preferences);
        KeysModel keysModel = keysModelPersistence.loadOrCreateNew();

        BurpConfigPersistence burpConfigPersistence = new BurpConfigPersistence(preferences);
        BurpConfig burpConfig = burpConfigPersistence.loadOrCreateNew();

        UserInterface userInterface = api.userInterface();
        Window suiteWindow = userInterface.swingUtils().suiteFrame();

        RstaFactory rstaFactory = new DefaultRstaFactory(userInterface, api.logging());
        PresenterStore presenters = new PresenterStore();

        BurpView burpView = new BurpView(
                suiteWindow,
                presenters,
                keysModelPersistence,
                keysModel,
                rstaFactory,
                burpConfigPersistence,
                burpConfig,
                userInterface
        );

        userInterface.registerSuiteTab(burpView.getTabCaption(), burpView.getUiComponent());

        userInterface.registerHttpRequestEditorProvider(editorCreationContext ->
                new RequestEditorView(
                        presenters,
                        rstaFactory,
                        editorCreationContext.editorMode() != READ_ONLY
                )
        );

        userInterface.registerHttpResponseEditorProvider(editorCreationContext ->
                new ResponseEditorView(
                        presenters,
                        rstaFactory,
                        editorCreationContext.editorMode() != READ_ONLY
                )
        );

        Proxy proxy = api.proxy();
        ProxyConfig proxyConfig = burpConfig.proxyConfig();
        ByteUtils byteUtils = api.utilities().byteUtils();

        ProxyHttpMessageHandler proxyHttpMessageHandler = new ProxyHttpMessageHandler(proxyConfig, byteUtils);
        proxy.registerRequestHandler(proxyHttpMessageHandler);
        proxy.registerResponseHandler(proxyHttpMessageHandler);

        ProxyWsMessageHandler proxyWsMessageHandler = new ProxyWsMessageHandler(proxyConfig, byteUtils);
        proxy.registerWebSocketCreationHandler(proxyWebSocketCreation ->
                proxyWebSocketCreation.proxyWebSocket().registerProxyMessageHandler(proxyWsMessageHandler)
        );

        Intruder intruder = api.intruder();
        intruder.registerPayloadProcessor(new JWSPayloadProcessor(burpConfig.intruderConfig()));
    }
}
