package template.activities;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.MenuItem;
import template.appInterface.RootLayout;
import template.codeGenerator.CodeGenerator;
import template.intent.AdapterViewItemClick;
import template.intent.CardViewItemClick;
import template.intent.FABIntent;
import template.intent.Intent;
import template.managers.ProjectHandler;
import template.attributeInspector.GridViewAttributes;
import template.attributeInspector.GridViewFragmentAttributes;
import template.managers.AttributeInspectorManager;
import template.managers.GraphHandler;
import template.appInterface.*;
import template.utils.DragControllerType;
import template.utils.Imports;
import template.utils.IntentType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class GridViewActivity extends DraggableActivity {

    private String classCustomTemplate;
    private String classArrayTemplate;
    private String layoutTemplate;
    private String customAdapterTemplate;
    private String cellArrayTemplate;
    private String modelTemplate;
    private String cellCustomTemplate;
    private String classCustomFragment;
    private String classArrayFragment;
    private GridViewAttributes activityInspector= null;
    private GridViewFragmentAttributes fragmentInspector = null;
    private CodeGenerator codeGenerator = new CodeGenerator();
    private String adapterType;
    private int columns;

    public GridViewActivity() throws IOException {
        super();
        //get activity and layout templates in String
        classCustomTemplate = codeGenerator.provideTemplateForName("templates/activityTemplates/GridViewCustomAdapterActivity");
        classCustomFragment = codeGenerator.provideTemplateForName("templates/activityTemplates/GridViewCustomAdapterFragment");
        layoutTemplate = codeGenerator.provideTemplateForName("templates/activityTemplates/GridViewLayout");
        customAdapterTemplate = codeGenerator.provideTemplateForName("templates/activityTemplates/CustomAdapter");
        modelTemplate = codeGenerator.provideTemplateForName("templates/activityTemplates/CustomAdapterModel");
        cellCustomTemplate = codeGenerator.provideTemplateForName("templates/activityTemplates/AdapterViewCustomItem");
        classArrayTemplate = codeGenerator.provideTemplateForName("templates/activityTemplates/GridViewArrayAdapterActivity");
        classArrayFragment = codeGenerator.provideTemplateForName("templates/activityTemplates/GridViewArrayAdapterFragment");
        cellArrayTemplate = codeGenerator.provideTemplateForName("templates/activityTemplates/AdapterViewArrayItem");

        FXMLLoader fxmlLoader = new FXMLLoader(
                getClass().getResource("DraggableActivity.fxml")
        );

        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();

        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    public boolean isFragment() {
        return super.isFragment();
    }


    @FXML
    private void initialize() {
        super.init();
        activityInspector = new GridViewAttributes();
        fragmentInspector = new GridViewFragmentAttributes();
        super.setName("GridViewActivity"+ GraphHandler.getInstance().getActivityIndex(DragControllerType.gridView,"GridViewActivity"));
        adapterType = "String";
        columns = 2;
        loadInspector();
    }

    public int getColumns() {
        return columns;
    }

    public void setColumns(int columns) {
        this.columns = columns;
    }

    public String getAdapterType() {
        return adapterType;
    }

    public void setAdapterType(String adapterType) {
        this.adapterType = adapterType;
    }

    @Override
    public String createJavaCode() throws IOException {
        String template = null;
        String imports = "";
        String itemClick = "";
        String extraId="";
        if (adapterType.equals("Custom")){
            template = classCustomTemplate;
            generateAdapter();
            generateModel();
        }else if(adapterType.equals("String")){
            template = classArrayTemplate;
        }
        template = template.replace("${ACTIVITY_NAME}",super.getName());
        template = template.replace("${ACTIVITY_LAYOUT}",generateLayoutName(super.getName()));
        template = template.replace("${PACKAGE}", ProjectHandler.getInstance().getPackage());
        //check if there is a fab intent
        String fabIntent="";
        if (super.getOutgoingIntentsForType(IntentType.fabClick).size()>0){

            Intent intent = super.getOutgoingIntentsForType(IntentType.fabClick).get(0);
            if(!intent.getExtraType().equals("None")){
                extraId = extraId.concat(((FABIntent)intent).getExtraIdDeclaration()+"\n");
            }
            fabIntent = fabIntent.concat(((FABIntent)intent).getIntentCode()+"\n");
            imports = imports.concat(Imports.FAB);

        }
        template = template.replace("${FAB}",fabIntent);
        if(super.getOutgoingIntentsForType(IntentType.itemClick).size()>0){
            imports = imports.concat(Imports.ADAPTER_VIEW);
            Intent intent = super.getOutgoingIntentsForType(IntentType.itemClick).get(0);
            if(!intent.getExtraType().equals("None")){
                extraId = extraId.concat(((AdapterViewItemClick)intent).getExtraIdDeclaration()+"\n");
            }
            itemClick = itemClick.concat(((AdapterViewItemClick)intent).getIntentCode()+"\n");



        }
        //intent receivers
        String receivers = "";
        int nReceiver = 1;
        for(Intent i : getIngoingIntents()){
            if(i.getExtraType()!=null && !i.getExtraType().equals("None")){
                receivers = receivers.concat(i.getExtraReceiver(nReceiver)+"\n");
                nReceiver++;
            }
        }

        //up navigation
        if(super.getIngoingIntentsForType(IntentType.itemClick).size()>0 ||
                super.getIngoingIntentsForType(IntentType.cardClick).size()>0 ){
            template = template.replace("${UP_NAVIGATION}","getSupportActionBar().setDisplayHomeAsUpEnabled(true);"+"\n");
        }else{
            template = template.replace("${UP_NAVIGATION}","");
        }

        if (super.getOutgoingIntentsForType(IntentType.fabClick).size()>0 ||
                super.getOutgoingIntentsForType(IntentType.itemClick).size()>0 ||
                !receivers.equals("")){
            imports = imports.concat(Imports.INTENT);
        }
        template = template.replace("${IMPORTS}","\n"+imports+"\n");
        template = template.replace("${INTENT}","\n"+itemClick+"\n");

        template = template.replace("${INTENT_EXTRA_ID}","\n"+extraId);
        if (!receivers.equals("")){
            template = template.replace("${INTENT_RECEIVER}","Intent intent = getIntent();\n"+receivers);
        }else{
            template = template.replace("${INTENT_RECEIVER}",receivers);
        }
        return template;
    }

    @Override
    public String createFragmentCode() throws IOException {
        String template = null;
        String imports = "";
        String itemClick = "";
        String extraId="";
        if (adapterType.equals("Custom")){
            template = classCustomFragment;
            generateAdapter();
            generateModel();
        }else if(adapterType.equals("String")){
            template = classArrayFragment;
        }
        template = template.replace("${ACTIVITY_NAME}",super.getName());
        template = template.replace("${ACTIVITY_LAYOUT}",generateLayoutName(super.getName()));
        template = template.replace("${PACKAGE}", ProjectHandler.getInstance().getPackage());
        //check if there is a fab intent
        String fabIntent="";
        if (super.getOutgoingIntentsForType(IntentType.fabClick).size()>0){

            Intent intent = super.getOutgoingIntentsForType(IntentType.fabClick).get(0);
            if(!intent.getExtraType().equals("None")){
                extraId = extraId.concat(((FABIntent)intent).getExtraIdDeclaration()+"\n");
            }
            fabIntent = fabIntent.concat(((FABIntent)intent).getIntentCode()+'\n');

            imports = imports.concat(Imports.FAB);

        }
        template = template.replace("${FAB}",fabIntent);
        if(super.getOutgoingIntentsForType(IntentType.itemClick).size()>0){
            imports = imports.concat(Imports.ADAPTER_VIEW);
            Intent intent = super.getOutgoingIntentsForType(IntentType.itemClick).get(0);
            if(!intent.getExtraType().equals("None")){
                extraId = extraId.concat(((AdapterViewItemClick)intent).getExtraIdDeclaration()+"\n");
            }
            itemClick = itemClick.concat(((AdapterViewItemClick)intent).getIntentCode()+"\n");

        }
        if (super.getOutgoingIntentsForType(IntentType.fabClick).size()>0 ||
                super.getOutgoingIntentsForType(IntentType.itemClick).size()>0){
            imports = imports.concat(Imports.INTENT);
        }
        template = template.replace("${IMPORTS}","\n"+imports+"\n");
        template = template.replace("${INTENT}","\n"+itemClick+"\n");
        template = template.replace("${INTENT_EXTRA_ID}","\n"+extraId);
        return template;
    }

    public void generateAdapter() throws IOException {
        String template = customAdapterTemplate;
        template = template.replace("${ACTIVITY_NAME}",super.getName());
        template = template.replace("${ACTIVITY_LAYOUT}",generateLayoutName(super.getName()));
        template = template.replace("${ITEM_LAYOUT}",generateLayoutName(super.getName())+"_cell");
        template = template.replace("${PACKAGE}", ProjectHandler.getInstance().getPackage());
        ProjectHandler projectHandler = ProjectHandler.getInstance();
        /*codeGenerator.generateJavaFile(projectHandler.getProjectPath()+"/app/src/main/java/"
                +projectHandler.getPackagePath()+"/adapter/",super.getName()+"Adapter.java",template);*/
        codeGenerator.generateJavaFile(projectHandler.getProjectPath()+"/app/src/main/java/"
                +projectHandler.getPackagePath()+"/adapter/"+super.getName()+"Adapter.java",template,super.getName()+"Adapter");
    }

    public void generateModel() throws IOException {
        String template = modelTemplate;
        template = template.replace("${ACTIVITY_NAME}",super.getName());
        template = template.replace("${PACKAGE}",ProjectHandler.getInstance().getPackage());
        ProjectHandler projectHandler = ProjectHandler.getInstance();
        /*codeGenerator.generateJavaFile(projectHandler.getProjectPath()+"/app/src/main/java/"
                +projectHandler.getPackagePath()+"/adapter/",super.getName()+"Model.java",template);*/
        codeGenerator.generateJavaFile(projectHandler.getProjectPath()+"/app/src/main/java/"
                +projectHandler.getPackagePath()+"/adapter/"+super.getName()+"Model.java",template,super.getName()+"Model");
    }

    public String createXMLCode() throws IOException {
        String template = layoutTemplate;
        template = template.replace("${ACTIVITY_NAME}",super.getName());
        template = template.replace("${PACKAGE}",ProjectHandler.getInstance().getPackage());
        template = template.replace("${N_COLUMNS}",Integer.toString(columns));
        if (super.getOutgoingIntentsForType(IntentType.fabClick).size()>0){
            template = template.replace("${FAB}","\n"+codeGenerator.provideTemplateForName("templates/intentTemplates/FABIntentLayout")+"\n");
        }else{
            template = template.replace("${FAB}","");
        }

        generateCell();

        return template;
    }

    public void generateCell() throws IOException {
        String template = null;
        if(adapterType.equals("Custom")){
            template = cellCustomTemplate;
        }else if(adapterType.equals("String")){
            template = cellArrayTemplate;
        }
        ProjectHandler projectHandler = ProjectHandler.getInstance();
        /*codeGenerator.generateXMLFile(projectHandler.getProjectPath()+"/app/src/main/res/layout/"
                ,generateLayoutName(super.getName())+"_cell.xml",template);*/
        codeGenerator.generateXMLFile(projectHandler.getProjectPath()+"/app/src/main/res/layout/"
                +generateLayoutName(super.getName())+"_cell.xml",template,generateLayoutName(super.getName())+"_cell");

    }

    @Override
    public void loadInspectorListeners(){
        activityInspector.createListeners(this);
        fragmentInspector.createListeners(this);
    }

    @Override
    public void loadInspector(){
        AttributeInspectorManager inspectorManager = AttributeInspectorManager.getInstance();
        if(isFragment()){
            fragmentInspector.fillValues(this);
            inspectorManager.loadActivityInspector(fragmentInspector,this);
        }else{
            activityInspector.fillValues(this);
            inspectorManager.loadActivityInspector(activityInspector,this);
        }

    }

    @Override
    public void isInitialActivity(boolean isInitial){
        activityInspector.setCheckBox(isInitial);
    }

    @Override
    public String getManifest() throws IOException {
        String attributes ="";
        String manifest = codeGenerator.provideTemplateForName("templates/activityTemplates/ManifestActivity");
        manifest = manifest.replace("${ACTIVITY}",super.getName());
        attributes = attributes.concat(" android:label=\""+super.getName()+"\"");
        if (IsInitialActivity.getInstance().isInitialActivity(this)){
            manifest = manifest.replace("${INTENT_FILTER}","\n"+codeGenerator.provideTemplateForName("templates/activityTemplates/IntentFilterLauncher")+"\n\t\t");
        }else {
            manifest = manifest.replace("${INTENT_FILTER}","");
        }

        //up navigation
        if(super.getIngoingIntentsForType(IntentType.itemClick).size()==1){
            AdapterViewItemClick i = (AdapterViewItemClick)super.getIngoingIntentsForType(IntentType.itemClick).get(0);
            if(i.getBelongingLink().getSource().isFragment()){
                attributes = attributes.concat("\nandroid:parentActivityName=\"."+getContainerActivity(i.getBelongingLink().getSource()).getName()+"\"");
            }else{
                attributes = attributes.concat("\nandroid:parentActivityName=\"."+i.getBelongingLink().getSource().getName()+"\"");
            }
        } else if(super.getIngoingIntentsForType(IntentType.cardClick).size()==1){
            CardViewItemClick i = (CardViewItemClick) super.getIngoingIntentsForType(IntentType.cardClick).get(0);
            if(i.getBelongingLink().getSource().isFragment()){
                attributes = attributes.concat("\nandroid:parentActivityName=\"."+getContainerActivity(i.getBelongingLink().getSource()).getName()+"\"");
            }else{
                attributes = attributes.concat("\nandroid:parentActivityName=\"."+i.getBelongingLink().getSource().getName()+"\"");
            }
        }
        manifest = manifest.replace("${ATTRIBUTES}",attributes);
        return manifest;
    }

    //generate layout name from object name. Ex: MyClass -> _my_class
    @Override
    public String generateLayoutName(String objectName){
        Scanner in = new Scanner(objectName);
        String out = "";
        String x = in.next();
        int z = x.length();
        for(int y = 0; y < z; y++){
            if(Character.isUpperCase(x.charAt(y))){
                out = out+"_"+(Character.toLowerCase(x.charAt(y)));

            }else{
                out = out+x.charAt(y);
            }
        }
        if(isFragment()){
            return "fragment"+out;
        }else{
            return "activity"+out;
        }

    }

    @Override
    public java.util.List<MenuItem> getMenuItems(RootLayout root, DragContainer container, DraggableActivity target){
        List<MenuItem> items = new ArrayList<MenuItem>();
        if(!target.isFragment()){

            if(super.getOutgoingIntentsForType(IntentType.fabClick).size() ==0){
                MenuItem item1 = new MenuItem("FAB Intent");
                item1.setOnAction(new EventHandler<ActionEvent>() {

                    @Override
                    public void handle(ActionEvent event) {
                        IntentType intentType = IntentType.fabClick;
                        try {
                            root.createLink(container,intentType);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
                items.add(item1);
            }
            if(super.getOutgoingIntentsForType(IntentType.itemClick).size() ==0 &&
                    target.getIngoingIntentsForType(IntentType.itemClick).size() ==0 &&
                    target.getIngoingIntentsForType(IntentType.cardClick).size() ==0){
                MenuItem item2 = new MenuItem("Item Click");
                item2.setOnAction(new EventHandler<ActionEvent>() {

                    @Override
                    public void handle(ActionEvent event) {
                        IntentType intentType = IntentType.itemClick;
                        try {
                            root.createLink(container,intentType);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
                items.add(item2);
            }
        }

        return items;
    };
}
