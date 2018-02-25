package template.sample;

/**
 * Created by utente on 09/02/2018.
 */
public interface Imports {
    String BUTTON = "import android.widget.Button;";
    String VIEW ="import android.view.View;";
    String INTENT ="import android.content.Intent;";
    String FAB ="import android.support.design.widget.FloatingActionButton;";
    String ADAPTER_VIEW ="import android.widget.AdapterView;";
    String CLICK_LISTENER="import ${PACKAGE}.adapter.${ACTIVITY_NAME}ClickListener;";
    String RESULT_OK ="import static android.app.Activity.RESULT_OK;";
}
