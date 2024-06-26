package App;

import AmbrosiaUI.Widgets.Events.WidgetEventListener;
import Dissimulo.InternalValue;
import Dissimulo.Interpreter;
import Dissimulo.InterpreterContext;
import Dissimulo.InterpreterFunction;
import AmbrosiaUI.Prompts.*;
import AmbrosiaUI.Widgets.*;
import AmbrosiaUI.Widgets.Button;
import AmbrosiaUI.Widgets.Editors.HexEditor.HexEditor;
import AmbrosiaUI.Widgets.Editors.PIconEditor.PIconEditor;
import AmbrosiaUI.Widgets.Frame;
import AmbrosiaUI.Widgets.Icons.PathImage;
import AmbrosiaUI.Widgets.Label;
import AmbrosiaUI.Widgets.Scrollbar;
import AmbrosiaUI.Utility.*;
import AmbrosiaUI.Widgets.DropdownMenu.DropdownMenu;
import AmbrosiaUI.Widgets.DropdownMenu.DropdownMenuItem;
import AmbrosiaUI.Widgets.Placements.HorizontalPlacement;
import AmbrosiaUI.Widgets.Editors.EditorLike;
import AmbrosiaUI.Widgets.Editors.TextEditor.TextEditor;
import AmbrosiaUI.Widgets.TabbedFrame.TabbedFrame;
import AmbrosiaUI.Widgets.TabbedFrame.TabbedFrameTab;
import AmbrosiaUI.Widgets.Window;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;

/**
 * The full ap put together using ambrosiaUI
 */
public class Root extends Window {
    private EditorLike editorInFocus;

    private final TabbedFrame editorSpace;
    private final Scrollbar scrollbar;

    private final Button save_file;

    private Settings settingsWindow;
    private ExtensionsManager extensionsManager;

    private Interpreter interpreter = new Interpreter();

    private ArrayList<EditorChangedListener> editorChangedListeners = new ArrayList<>();

    private HorizontalPlacement header_placement;
    private HorizontalPlacement mainPlacement;
    public Root() {
        super();
        initializeInterpreter();
        mainPlacement = new HorizontalPlacement(theme);

        coreFrame.setChildrenPlacement(mainPlacement);

        header_placement = new HorizontalPlacement(theme);
        coreHeader.setChildrenPlacement(header_placement);

        FolderView fw = new FolderView(theme){
            @Override
            public void initialize() {
                super.initialize();
                FontMetrics fm = fontsizecanvas.getFontMetrics(theme.getFontByName("small"));
                corePlacement.setRowTemplateFromString("5px auto " + (fm.getAscent()+fm.getDescent()+5) + "px");

                pathDisplayFrame.setMargin(0);
                corePlacement.add(pathDisplayFrame, 2, 0, 1,1);
            }

            @Override
            protected void fileSelected(String file) {
                boolean found = false;

                for(TabbedFrameTab tab: editorSpace.getTabs()){
                    EditorLike editor = ((EditorLike) tab.getBoundElement().getChildrenPlacement().getChildren().get(0).getBoundElement());

                    if(editor.getCurrentFile() != null && editor.getCurrentFile().equals(file)){
                        Root.this.setCurrentEditor(editor);
                        editorSpace.selectTab(editorSpace.getTabs().indexOf(tab));
                        found = true;
                        break;
                    }
                }

                if(!found){
                    EditorLike newEditor;
                    if(file.matches(".*\\.pimg")){
                        newEditor = addPIconEditor();
                    }
                    else {
                        newEditor = addEditor();
                    }
                    newEditor.openFile(file);
                    editorSpace.selectTab(editorSpace.getTabs().size()-1);

                    if(newEditor.getCurrentFile() == null){
                        editorSpace.removeTab(editorSpace.getTabs().size()-1);
                        return;
                    }
                    newEditor.onCurrentFileChanged();
                    setCurrentEditor(newEditor);
                }
                Root.this.update();
            }

            @Override
            public void setPath(String path) {
                super.setPath(path);
                Root.this.update();
            }

            @Override
            protected void updatePath() {
                pathDisplayPlacement.clear();
                String name = path;

                Label temp = new Label(name,"small",0,0,5);
                temp.setHoverEffectDisabled(true);
                pathDisplayPlacement.add(temp, new UnitValue(getStringWidth(name,theme.getFontByName("small"))+10, UnitValue.Unit.PIXELS));
            }
        };
        mainPlacement.add(fw, new UnitValue(0, UnitValue.Unit.FIT));
        fw.setCanAddFolders(false);
        fw.setNavigationOnFolderClick(false);
        fw.initialize();

        fw.addAllowed(".*");
        fw.setPath(new File("").getAbsolutePath());
        //fw.setPath("/home/hades/IdeaProjects/TextEditor");

        Button new_file = new Button("New", "small", 0,0,4) {
            @Override
            public void onMouseClicked(MouseEvent e) {
                CreateFilePrompt f = new CreateFilePrompt(theme){
                    @Override
                    public void onSubmited(PromptResult result) {
                        setCurrentEditor(addEditor());

                        editorInFocus.setCurrentFile(result.getContent());
                        editorInFocus.saveToCurrentlyOpenFile();
                        fw.updateFiles();
                        win.update();
                    }
                };
                f.setPath(fw.getPath());
                f.addAllowed(".*");
                f.ask();
            }
        };
        new_file.setBind(panel, KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.CTRL_DOWN_MASK));
        new_file.setTextPlacement(AdvancedGraphics.Side.LEFT);

        save_file = new Button("Save", "small", 0,0,4) {
            @Override
            public void onMouseClicked(MouseEvent e) {
                editorInFocus.saveToCurrentlyOpenFile();
                editorInFocus.openFile(editorInFocus.getCurrentFile());
            }
        };
        save_file.setBind(panel, KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK));

        Button open_file = new Button("In Current Editor", "small", 0,0,4) {
            @Override
            public void onMouseClicked(MouseEvent e) {
                FilePrompt f = new FilePrompt(theme){
                    @Override
                    public void onSubmited(PromptResult result) {
                        editorInFocus.openFile(result.getContent());
                        Root.this.update();
                    }
                };
                f.setPath(fw.getPath());
                f.addAllowed(editorInFocus.getAllowedFiles());
                f.ask();
            }
        };
        open_file.setTextPlacement(AdvancedGraphics.Side.LEFT);

        Button open_file_in_new_editor = new Button("In New Text Editor", "small", 0,0,4) {
            @Override
            public void onMouseClicked(MouseEvent e) {
                FilePrompt f = new FilePrompt(theme){
                    @Override
                    public void onSubmited(PromptResult result) {
                        Root.this.setCurrentEditor(addEditor());
                        editorInFocus.openFile(result.getContent());
                        Root.this.update();
                    }
                };
                f.setPath(fw.getPath());
                f.addAllowed(new TextEditor().getAllowedFiles());
                f.ask();
            }
        };
        open_file_in_new_editor.setTextPlacement(AdvancedGraphics.Side.LEFT);

        Button open_file_in_new_editor_hex = new Button("In New Hex Editor", "small", 0,0,4) {
            @Override
            public void onMouseClicked(MouseEvent e) {
                FilePrompt f = new FilePrompt(theme){
                    @Override
                    public void onSubmited(PromptResult result) {
                        Root.this.setCurrentEditor(addHexEditor());
                        editorInFocus.openFile(result.getContent());
                        Root.this.update();
                    }
                };
                f.setPath(fw.getPath());
                f.addAllowed(new HexEditor().getAllowedFiles());
                f.ask();
            }
        };
        open_file_in_new_editor_hex.setTextPlacement(AdvancedGraphics.Side.LEFT);

        Button open_file_in_new_editor_icon = new Button("In New Icon Editor", "small", 0,0,4) {
            @Override
            public void onMouseClicked(MouseEvent e) {
                FilePrompt f = new FilePrompt(theme){
                    @Override
                    public void onSubmited(PromptResult result) {
                        Root.this.setCurrentEditor(addPIconEditor());
                        editorInFocus.openFile(result.getContent());
                        Root.this.update();
                    }
                };
                f.setPath(fw.getPath());
                f.addAllowed(new PIconEditor().getAllowedFiles());
                f.ask();
            }
        };
        open_file_in_new_editor_icon.setTextPlacement(AdvancedGraphics.Side.LEFT);

        Button open_folder = new Button("Open Folder", "small", 0,0,4) {
            @Override
            public void onMouseClicked(MouseEvent e) {
                FolderPrompt f = new FolderPrompt(theme){
                    @Override
                    public void onSubmited(PromptResult result) {
                        fw.setPath(result.getContent());
                        fw.updateFiles();
                        Root.this.update();
                    }
                };
                f.setPath(fw.getPath());
                f.ask();
            }
        };
        open_folder.setTextPlacement(AdvancedGraphics.Side.LEFT);

        save_file.setDisabled(true);
        save_file.setTextPlacement(AdvancedGraphics.Side.LEFT);

        Button save_file_as = new Button("Save as..", "small", 0,0,4) {
            @Override
            public void onMouseClicked(MouseEvent e) {
                CreateFilePrompt f = new CreateFilePrompt(theme){
                    @Override
                    public void onSubmited(PromptResult result) {
                        editorInFocus.setCurrentFile(result.getContent());
                        editorInFocus.saveToCurrentlyOpenFile();
                        fw.updateFiles();
                    }
                };
                f.setPath(fw.getPath());
                f.addAllowed(".*");
                f.ask();
            }
        };
        save_file_as.setBind(panel, KeyStroke.getKeyStroke(KeyEvent.VK_S,  KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK));
        save_file_as.setTextPlacement(AdvancedGraphics.Side.LEFT);
        Button settings = new Button("Settings", "small", 0,0,4) {
            @Override
            public void onMouseClicked(MouseEvent e) {
                settingsWindow.show();
            }
        };
        settings.setTextPlacement(AdvancedGraphics.Side.LEFT);

        Button extensions = new Button("Extensions", "small", 0,0,4) {
            @Override
            public void onMouseClicked(MouseEvent e) {
                extensionsManager.show();
            }
        };
        extensions.setTextPlacement(AdvancedGraphics.Side.LEFT);

        DropdownMenu menu = new DropdownMenu("File", "small",0, 0, 4, new Size(200,30));
        menu.setzIndex(1);

        DropdownMenu tools = new DropdownMenu("Tools", "small",0,0,4,new Size(200,30));
        tools.setzIndex(1);

        Button open_console = new Button("Open Console", "small", 0,0,4){
            @Override
            public void onMouseClicked(MouseEvent e) {
                if(System.getProperty("os.name").startsWith("Windows")){
                    try {
                        Runtime rt = Runtime.getRuntime();
                        rt.exec("cmd.exe /k start", null, new File(fw.getPath()));
                    } catch (IOException i) {
                        i.printStackTrace();
                    }
                }
                else if(System.getProperty("os.name").startsWith("Linux")){
                    try {
                        Runtime.getRuntime().exec("gnome-terminal --working-directory=" + fw.getPath());
                    } catch (IOException i) {
                        i.printStackTrace();
                    }
                }
            }
        };
        open_console.setBind(panel, KeyStroke.getKeyStroke(KeyEvent.VK_T,  KeyEvent.CTRL_DOWN_MASK));
        open_console.setTextPlacement(AdvancedGraphics.Side.LEFT);

        DropdownMenu openMenu = new DropdownMenu("Open..", "small", 0, 0, 4, new Size(200,30));
        openMenu.setzIndex(1);
        openMenu.setTextPlacement(AdvancedGraphics.Side.LEFT);

        header_placement.add(menu, new UnitValue(0, UnitValue.Unit.FIT));
        header_placement.add(tools, new UnitValue(0, UnitValue.Unit.FIT));

        tools.addMenuItem(new DropdownMenuItem(open_console));

        menu.addMenuItem(new DropdownMenuItem(new_file));
        menu.addMenuItem(new DropdownMenuItem()); // Spacer
        menu.addMenuItem(new DropdownMenuItem(openMenu));
        menu.addMenuItem(new DropdownMenuItem(open_folder));
        menu.addMenuItem(new DropdownMenuItem()); // Spacer
        menu.addMenuItem(new DropdownMenuItem(save_file));
        menu.addMenuItem(new DropdownMenuItem(save_file_as));
        menu.addMenuItem(new DropdownMenuItem()); // Spacer
        menu.addMenuItem(new DropdownMenuItem(settings));
        menu.addMenuItem(new DropdownMenuItem(extensions));

        openMenu.addMenuItem(new DropdownMenuItem(open_file));
        openMenu.addMenuItem(new DropdownMenuItem(open_file_in_new_editor));
        openMenu.addMenuItem(new DropdownMenuItem(open_file_in_new_editor_hex));
        openMenu.addMenuItem(new DropdownMenuItem(open_file_in_new_editor_icon));

        scrollbar = new Scrollbar("primary", null, UnitValue.Direction.VERTICAL);


        editorSpace = new TabbedFrame("primary", 0) {
            @Override
            public void selectTab(int index) {
                super.selectTab(index);

                EditorLike editor = ((EditorLike) this.getTabs().get(index).getBoundElement().getChildrenPlacement().getChildren().get(0).getBoundElement());

                if(editor != editorInFocus) {
                    setCurrentEditor(editor);
                }
                editor.onCurrentFileChanged();
                Root.this.update();
            }
        };
        mainPlacement.add(editorSpace,new UnitValue(0, UnitValue.Unit.AUTO));
        editorSpace.initialize();

        setCurrentEditor(addEditor());
        scrollbar.setController(editorInFocus.getScrollController());

        //editorSpacePlacement.add(secondaryEditor, new UnitValue(0, UnitValue.Unit.AUTO));

        mainPlacement.add(scrollbar, new UnitValue(20, UnitValue.Unit.PIXELS));
        //core.resize(Size.fromDimension(this.getSize()));

        panel.setFocusTraversalKeysEnabled(false); // Stop taking away my TAB ://
        this.bindEvents();

        initializeSubwindows();
    }


    private void initializeInterpreter(){
        interpreter.addFunction("addEditor", new InterpreterFunction(interpreter) {
            @Override
            protected InternalValue internalExecute(ArrayList<InternalValue> values, InterpreterContext context) {
                addEditor();
                return new InternalValue(InternalValue.ValueType.NONE);
            }
        });
        interpreter.addFunction("setThemeColor", new InterpreterFunction(interpreter){
            @Override
            protected InternalValue internalExecute(ArrayList<InternalValue> values, InterpreterContext context) {
                values = replaceVariablesWithValues(values,context);
                if(values.size() != 4){
                    return new InternalValue(InternalValue.ValueType.NONE);
                }

                Color clr = new Color(
                    Integer.parseInt(values.get(1).getValue()),
                    Integer.parseInt(values.get(2).getValue()),
                    Integer.parseInt(values.get(3).getValue())
                );

                theme.setColor(values.get(0).getValue(), clr);

                try {
                    Root.this.update();
                }catch (Exception ignored){

                }

                return new InternalValue(InternalValue.ValueType.NONE);
            }
        });
        interpreter.addFunction("prompt", new InterpreterFunction(interpreter){
            @Override
            protected InternalValue internalExecute(ArrayList<InternalValue> values, InterpreterContext context) {
                ArrayList<InternalValue> internalValues = replaceStringObjectsWithStrings(values, context);

                if(internalValues.size() != 2 || internalValues.get(0).getType() != InternalValue.ValueType.STRING || internalValues.get(1).getType() != InternalValue.ValueType.ID){
                    Logger.printError("Invalid arguments for prompt.");
                    return new InternalValue(InternalValue.ValueType.NONE);
                }

                if(!context.hasFunction(internalValues.get(1).getValue())){
                    Logger.printError("Function '" + internalValues.get(1).getValue() + "' doesnt exist in this context.");
                    return new InternalValue(InternalValue.ValueType.NONE);
                }

                TextPrompt p = new TextPrompt(theme,internalValues.get(0).getValue()){
                    @Override
                    public void onSubmited(PromptResult result) {
                        ArrayList<InternalValue> val = new ArrayList<>();
                        val.add(new InternalValue(InternalValue.ValueType.STRING,result.getContent()));

                        context.getFunction(internalValues.get(1).getValue()).externalExecute(val, context);
                    }
                };
                p.ask();

                return new InternalValue(InternalValue.ValueType.NONE);
            }
        });
        interpreter.addFunction("update", new InterpreterFunction(interpreter){
            @Override
            protected InternalValue internalExecute(ArrayList<InternalValue> values, InterpreterContext context) {
                try {
                    Root.this.update();
                }catch (Exception ignored){

                }
                return new InternalValue(InternalValue.ValueType.NONE);
            }
        });
        interpreter.addFunction("getHeader", new InterpreterFunction(interpreter){
            @Override
            protected InternalValue internalExecute(ArrayList<InternalValue> values, InterpreterContext context) {
                return Root.this.interpreter.generateReferenceForObject(header_placement);
            }
        });
        interpreter.addFunction("getCoreHeader", new InterpreterFunction(interpreter){
            @Override
            protected InternalValue internalExecute(ArrayList<InternalValue> values, InterpreterContext context) {
                return Root.this.interpreter.generateReferenceForObject(coreHeader);
            }
        });
        interpreter.addFunction("addMouseClickedListener", new InterpreterFunction(interpreter){
            @Override
            protected InternalValue internalExecute(ArrayList<InternalValue> values, InterpreterContext context) {
                ArrayList<InternalValue> temp = replaceStringObjectsWithStrings(values, context);
                ArrayList<InternalValue> internalValues = replaceVariablesWithValues(temp ,context);

                if(internalValues.size() != 2 || internalValues.get(0).getType() != InternalValue.ValueType.REFERENCE || internalValues.get(1).getType() != InternalValue.ValueType.ID){
                    Logger.printError("Invalid arguments for addMouseClickedListener.");
                    return new InternalValue(InternalValue.ValueType.NONE);
                }

                if(!context.hasFunction(internalValues.get(1).getValue())){
                    Logger.printError("Function '" + internalValues.get(1).getValue() + "' doesnt exist in this context.");
                    return new InternalValue(InternalValue.ValueType.NONE);
                }

                Object obj = interpreter.getStoredObject(internalValues.get(0).getValue());

                ((Widget) obj).addEventListener(new WidgetEventListener() {
                    @Override
                    public void onMouseDragged(MouseEvent e) {

                    }

                    @Override
                    public void onMouseMoved(MouseEvent e) {

                    }

                    @Override
                    public void onMouseClicked(MouseEvent e) {
                        context.getFunction(internalValues.get(1).getValue()).externalExecute(new ArrayList<>(), context);
                    }

                    @Override
                    public void onMousePressed(MouseEvent e) {

                    }

                    @Override
                    public void onMouseReleased(MouseEvent e) {

                    }

                    @Override
                    public void onKeyPressed(KeyEvent keyEvent) {

                    }

                    @Override
                    public void onKeyReleased(KeyEvent keyEvent) {

                    }

                    @Override
                    public void onPasted(String pastedData) {

                    }

                    @Override
                    public void onMouseWheel(MouseWheelEvent event) {

                    }
                });



                return new InternalValue(InternalValue.ValueType.NONE);
            }
        });
        interpreter.addFunction("addEditorChangedListener", new InterpreterFunction(interpreter){
            @Override
            protected InternalValue internalExecute(ArrayList<InternalValue> values, InterpreterContext context) {
                ArrayList<InternalValue> internalValues = replaceStringObjectsWithStrings(values, context);

                if(internalValues.size() != 1 || internalValues.get(0).getType() != InternalValue.ValueType.ID){
                    Logger.printError("Invalid arguments for addMouseClickedListener.");
                    return new InternalValue(InternalValue.ValueType.NONE);
                }

                if(!context.hasFunction(internalValues.get(0).getValue())){
                    Logger.printError("Function '" + internalValues.get(1).getValue() + "' doesnt exist in this context.");
                    return new InternalValue(InternalValue.ValueType.NONE);
                }

                editorChangedListeners.add(new EditorChangedListener() {
                    @Override
                    public void onChanged(EditorLike editorLike) {
                        ArrayList<InternalValue> val = new ArrayList<>();
                        val.add(interpreter.generateReferenceForObject(editorLike));
                        context.getFunction(internalValues.get(0).getValue()).externalExecute(val, context);
                    }
                });



                return new InternalValue(InternalValue.ValueType.NONE);
            }
        });
        interpreter.addFunction("isCurrentEditorText", new InterpreterFunction(interpreter){
            @Override
            protected InternalValue internalExecute(ArrayList<InternalValue> values, InterpreterContext context) {
                return new InternalValue(InternalValue.ValueType.BOOL, (editorInFocus instanceof TextEditor)+"");
            }
        });

        interpreter.addFunction("getEditorContent", new InterpreterFunction(interpreter){
            @Override
            protected InternalValue internalExecute(ArrayList<InternalValue> values, InterpreterContext context) {
                if(editorInFocus instanceof TextEditor){
                    return interpreter.generateReferenceForObject(((TextEditor)editorInFocus).getFullContent());
                }
                return interpreter.generateReferenceForObject("");
            }
        });
    }
    public TextEditor addEditor(){
        TabbedFrameTab tab = editorSpace.addTab("unnamed *");

        TextEditor editor = new TextEditor() {
            @Override
            public void onMouseClicked(MouseEvent e) {
                super.onMouseClicked(e);
                Root.this.setCurrentEditor(this);
            }

            @Override
            public void onCurrentFileChanged() {
                super.onCurrentFileChanged();

                if(editorInFocus != this){
                    return;
                }
                //System.out.println("A"+ editorInFocus.getText().hasFile());
                save_file.setDisabled(!this.hasFile());

                if(this.hasFile()) {
                    tab.setName(new File(getCurrentFile()).getName());
                }
            }
        };
        editor.onCurrentFileChanged();

        Frame tabFrame = (Frame) tab.getBoundElement();
        HorizontalPlacement temp = new HorizontalPlacement(theme);
        tabFrame.setChildrenPlacement(temp);
        temp.add(editor,new UnitValue(0, UnitValue.Unit.AUTO));

        return editor;
    }

    public HexEditor addHexEditor(){
        TabbedFrameTab tab = editorSpace.addTab("unnamed *");

        HexEditor editor = new HexEditor() {
            @Override
            public void onMouseClicked(MouseEvent e) {
                super.onMouseClicked(e);
                Root.this.setCurrentEditor(this);
            }

            @Override
            public void onCurrentFileChanged() {
                //System.out.println("A"+ editorInFocus.getText().hasFile());
                if(editorInFocus != this){
                    return;
                }

                if(this.hasFile()) {
                    tab.setName(new File(getCurrentFile()).getName());
                }
                save_file.setDisabled(!this.hasFile());
            }
        };
        editor.onCurrentFileChanged();

        Frame tabFrame = (Frame) tab.getBoundElement();
        HorizontalPlacement temp = new HorizontalPlacement(theme);
        tabFrame.setChildrenPlacement(temp);
        temp.add(editor,new UnitValue(0, UnitValue.Unit.AUTO));

        return editor;
    }

    public PIconEditor addPIconEditor(){
        TabbedFrameTab tab = editorSpace.addTab("unnamed *");

        PIconEditor editor = new PIconEditor("primary",0) {
            @Override
            public void onMouseClicked(MouseEvent e) {
                super.onMouseClicked(e);
                Root.this.setCurrentEditor(this);
            }

            @Override
            public void onCurrentFileChanged() {
                if(editorInFocus != this){
                    return;
                }

                //System.out.println("A"+ editorInFocus.getText().hasFile());
                if(this.hasFile()) {
                    tab.setName(new File(getCurrentFile()).getName());
                }
                save_file.setDisabled(!this.hasFile());
            }
        };
        editor.onCurrentFileChanged();

        Frame tabFrame = (Frame) tab.getBoundElement();
        HorizontalPlacement temp = new HorizontalPlacement(theme);
        tabFrame.setChildrenPlacement(temp);
        temp.add(editor,new UnitValue(0, UnitValue.Unit.AUTO));

        return editor;
    }

    public void initializeSubwindows(){
        settingsWindow = new Settings(theme,this);
        extensionsManager = new ExtensionsManager(theme,this);
    }

    public void bindEvents(){
        panel.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                if(editorInFocus.dontAutoScroll()){
                    Root.this.update();
                    return;
                }
                editorInFocus.getScrollController().setScrollY(Math.max(0, editorInFocus.getScrollController().getScrollY() + e.getWheelRotation()*25));
                Root.this.update();
            }
        });

        Keybind save = new Keybind("Save", panel, KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK)) {
            @Override
            public void activated(ActionEvent e) {
                if(editorInFocus.hasFile()) {
                    editorInFocus.saveToCurrentlyOpenFile();
                }
            }
        };

        Keybind reloadEditor = new Keybind("ReloadEditor", panel, KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.CTRL_DOWN_MASK)) {
            @Override
            public void activated(ActionEvent e) {
                if (editorInFocus != null){
                    editorInFocus.reload();
                }
                PathImage.reloadAllImagesFromFiles();
                /*if(editorSpacePlacement.getChildren().size() < 2){
                    return;
                }
                editorSpacePlacement.remove((Widget) editorInFocus);
                editorInFocus = (EditorLike) editorSpacePlacement.getChildren().get(0).getBoundElement();
                scrollbar.setController(editorInFocus.getScrollController());*/
            }
        };

        Keybind revert = new Keybind("Revert", panel, KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_DOWN_MASK)){
            @Override
            public void activated(ActionEvent e) {
                editorInFocus.revert();
            }
        };
    }

    public void setCurrentEditor(EditorLike editor){
        editorInFocus = editor;
        element_in_focus = (Widget) editorInFocus;

        for(TabbedFrameTab tab: editorSpace.getTabs()){
            EditorLike editorf = ((EditorLike) tab.getBoundElement().getChildrenPlacement().getChildren().get(0).getBoundElement());

            if(editorf == editorInFocus){
                editorSpace.selectTab(editorSpace.getTabs().indexOf(tab));
                break;
            }
        }

        scrollbar.setController(editorInFocus.getScrollController());
        onEditorChanged();
        update();
    }

    public void onEditorChanged(){
        for(EditorChangedListener ls: editorChangedListeners){
            ls.onChanged(this.editorInFocus);
        }
    }

    @Override
    public void close() {
        destroy();
        settingsWindow.destroy();
    }

    public Interpreter getInterpreter() {
        return interpreter;
    }

    public void openFile(String filename){
        editorInFocus.openFile(filename);
    }
}
