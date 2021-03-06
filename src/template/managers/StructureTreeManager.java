package template.managers;

import javafx.scene.Node;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import template.appInterface.DraggableActivity;
import template.intent.Intent;
import template.appInterface.Link;
import template.appInterface.TreeItemParameter;

import java.util.ArrayList;
import java.util.List;

public class StructureTreeManager {
    private static StructureTreeManager instance = null;
    private TreeView<TreeItemParameter> structureTree = null;
    private TreeItem<TreeItemParameter> rootItem = null;
    private AnchorPane graph;

    public static StructureTreeManager getInstance() {

        if(instance == null) {
            instance = new StructureTreeManager();
        }
        return instance;
    }

    private StructureTreeManager() {
    }

    public TreeView<TreeItemParameter> getStructureTree() {
        return structureTree;
    }

    public void setStructureTree(TreeView<TreeItemParameter> structureTree) {
        this.structureTree = structureTree;
    }

    public TreeItem<TreeItemParameter> getRootItem() {
        return rootItem;
    }

    public void setRootItem(TreeItem<TreeItemParameter> rootItem) {
        this.rootItem = rootItem;
    }

    public void setGraph(AnchorPane graph) {
        this.graph = graph;
    }

    //delete item and its children from tree
    public void deleteSubTree(TreeItem<TreeItemParameter> root){
        Node n = searchById(root.getValue().getId());
        if (n instanceof DraggableActivity){
            //if activity is target of any link, search that link in the tree and delete it
            //this because in the tree links are children only of the source not the target
            for(Link l : ((DraggableActivity) n).getAnchoredLinks()){
                if (l.getTarget().getId().equals(root.getValue().getId())){
                    TreeItem<TreeItemParameter> itemLink = searchTreeItemById(l.getId(),rootItem);
                    boolean removed = itemLink.getParent().getChildren().remove(itemLink);
                }
            }
        }
        boolean removed = root.getParent().getChildren().remove(root);
    }

    public void addLinkToTree (Link link, DraggableActivity source,DraggableActivity target){

        TreeItem<TreeItemParameter> parentActivity = searchTreeItemById(source.getId(),rootItem);
        //add link to tree (to source activity node)
        TreeItem<TreeItemParameter> item = new TreeItem<TreeItemParameter>(new TreeItemParameter("Link to "+target.getName(),link.getId()),
                new ImageView(new Image(getClass().getResourceAsStream("/icons/link2.jpg"))));
        parentActivity.getChildren().add(item);
        int intentListSize = link.getIntentsList().size();
        //add intent to parent link
        Intent intent = link.getIntentsList().get(intentListSize-1); //ultimo intent aggiunto si trova in coda alla lista
        TreeItem<TreeItemParameter> itemIntent = new TreeItem<TreeItemParameter>(new TreeItemParameter(intent.getName(),intent.getId()),
                new ImageView(new Image(getClass().getResourceAsStream("/icons/intent2.jpg"))));
        item.getChildren().add(itemIntent);
    }

    public void addIntentToTree(Link link){
        TreeItem<TreeItemParameter> parentLink = searchTreeItemById(link.getId(),rootItem);
        int intentListSize = link.getIntentsList().size();
        //add intent to parent link
        Intent intent = link.getIntentsList().get(intentListSize-1); //ultimo intent aggiunto si trova in coda alla lista
        TreeItem<TreeItemParameter> itemIntent = new TreeItem<TreeItemParameter>(new TreeItemParameter(intent.getName(),intent.getId()),
                new ImageView(new Image(getClass().getResourceAsStream("/icons/intent2.jpg"))));
        parentLink.getChildren().add(itemIntent);
    }

    public TreeItem<TreeItemParameter> searchTreeItemById(String id,TreeItem<TreeItemParameter> searchRoot ){
        TreeItem<TreeItemParameter> foundItem = null;
        if (searchRoot != null){

            if (searchRoot.getValue().getId().equals(id)){
                return searchRoot;
            }else{
                for(TreeItem<TreeItemParameter> i : searchRoot.getChildren()){
                    foundItem = searchTreeItemById(id,i);
                    if (foundItem!=null)
                        break;
                }
            }
        }


        return foundItem;
    }

    public Node searchById (String id){
        Node node = null;
        for (Node n : graph.getChildren()){
            if (n.getId().equals(id)){
                node = n;
                return node;
            }
        }
        return node;
    }

    public void selectTreeItem(TreeItem<TreeItemParameter> item){
        structureTree.getSelectionModel().select(item);
    }

    public void deselectAll(){
        structureTree.getSelectionModel().select(null);
    }

    public void updateActivityName(String newName,String id){
        searchTreeItemById(id,rootItem).setValue(new TreeItemParameter(newName,id));
    }
    public void updateIntentName(String newName,String id){
        searchTreeItemById(id,rootItem).setValue(new TreeItemParameter(newName,id));
    }

    public List<DraggableActivity> getActivitiesFromTree(){
        List<DraggableActivity> activities = new ArrayList<DraggableActivity>();
        for(TreeItem<TreeItemParameter> item : rootItem.getChildren()){
            DraggableActivity activity = (DraggableActivity) searchById(item.getValue().getId());
            activities.add(activity);
        }
        return activities;
    }

}
