package edu.asu.commons.foraging.visualization.forestry;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JTextField;

import edu.asu.commons.foraging.graphics.TextureLoader;
import edu.asu.commons.foraging.util.BasicDialog;
import edu.asu.commons.foraging.util.BasicFileFilter;
import edu.asu.commons.foraging.visualization.forestry.vbo.ForestryView;
import edu.asu.commons.foraging.visualization.forestry.vbo.Tree;


/*
 * TreeGenerationDialog.java
 *
 * Created on July 17, 2006, 11:44 AM
 *
 * @author  dbhagvat
 */
public class TreeGenerationDialog extends BasicDialog {
    
    private static final long serialVersionUID = -211711719809797798L;
    ForestryView parentView;
    String dataFilePath = System.getProperty("user.dir") + File.separator + "data" + File.separator + "forestry";
    
	/** Creates new form TreeGenerationDialog */
    public TreeGenerationDialog(ForestryView parentView) {
    	this.parentView = parentView;
        initComponents();
        setSize(480, 150);
        parentView.centerChildDialog(this);
        setVisible(true);
        bindUsableComponents(getContentPane() );
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">
    private void initComponents() {
    	getContentPane().setLayout(null);        
        setTitle("Generate Tree");
        
//        //Trunk Base Radius
//        JLabel jLabel1 = new JLabel("Trunk Base Radius");        
//        getContentPane().add(jLabel1);
//        jLabel1.setBounds(10, 10, 110, 20);                
//        getContentPane().add(getBaseRadius());        
//                
//        //Trunk Top Radius
//        JLabel jLabel2 = new JLabel("Trunk Top Radius");        
//        getContentPane().add(jLabel2);
//        jLabel2.setBounds(10, 40, 110, 20);              
//        getContentPane().add(getTopRadius());
//        
//        //Trunk Length
//        JLabel jLabel3 = new JLabel("Trunk Length");        
//        getContentPane().add(jLabel3);
//        jLabel3.setBounds(10, 70, 110, 20);              
//        getContentPane().add(getTrunkLength());
//        
//        //Iterations
//        JLabel jLabel4 = new JLabel("Iterations");        
//        getContentPane().add(jLabel4);
//        jLabel4.setBounds(10, 100, 110, 20);              
//        getContentPane().add(getIterations());
        
        //Texture File
        JLabel jLabel5 = new JLabel("Texture File");        
        getContentPane().add(jLabel5);
        jLabel5.setBounds(10, 10, 110, 20);        
        getContentPane().add(getTextureFile());
        getContentPane().add(getBrowseButton());
        
        //Save File location
        JLabel jLabel6 = new JLabel("Save File");        
        getContentPane().add(jLabel6);
        jLabel6.setBounds(10, 40, 110, 20);        
        getContentPane().add(getSaveFile());
        getContentPane().add(getSaveBrowseButton());
                
        //Generate button
        getContentPane().add(getGenerate());
        
        pack();
    }
    // </editor-fold>
    
//    private JTextField getBaseRadius() {
//    	if (baseRadius == null){
//    		baseRadius = new JTextField("2.0");
//    		baseRadius.setBounds(130, 10, 70, 20);    		
//    	}
//    	return baseRadius;
//    }
//    
//    private JTextField getTopRadius() {
//    	if (topRadius == null){
//    		topRadius = new JTextField("1.5");
//    		topRadius.setBounds(130, 40, 70, 20);    		
//    	}
//    	return topRadius;
//    }
//    
//    private JTextField getTrunkLength() {
//    	if (trunkLength == null){
//    		trunkLength = new JTextField("16.0");
//    		trunkLength.setBounds(130, 70, 70, 20);    		
//    	}
//    	return trunkLength;
//    }
//    
//    private JTextField getIterations() {
//    	if (iterations == null){
//    		iterations = new JTextField("6");
//    		iterations.setBounds(130, 100, 70, 20);    		
//    	}
//    	return iterations;
//    }
    
    private JTextField getTextureFile() {
    	if (textureFile == null){
    		textureFile = new JTextField();//"\\tree.jpg");    		
    		//textureFile.setBounds(130, 130, 240, 20);
    		textureFile.setBounds(130, 10, 240, 20);
    	}
    	return textureFile;
    }
    
    private JButton getBrowseButton() {
    	if (browse == null) {  
    		browse = new JButton("Browse");    		
    		//browse.setBounds(380, 130, 79, 23);
    		browse.setBounds(380, 10, 79, 23);
    		
    		browse.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					BasicFileFilter filter = new BasicFileFilter();
				    filter.addExtension("jpg");
				    filter.addExtension("gif");
				    filter.setDescription("Image Files");
				    				    
					JFileChooser fileChooser = new JFileChooser();
					fileChooser.setFileFilter(filter);
					fileChooser.setCurrentDirectory(new File(dataFilePath));
	                int returnVal = fileChooser.showOpenDialog(TreeGenerationDialog.this);
	                if (returnVal == JFileChooser.APPROVE_OPTION) {
	                	getTextureFile().setText(fileChooser.getSelectedFile().getPath());
	                }
				}
			});
    	}
    	return browse;
    }
    
    private JTextField getSaveFile() {
    	if (saveFile == null){
    		saveFile = new JTextField(dataFilePath + File.separator + "tree1.tree");
    		//saveFile.setBounds(130, 160, 240, 20);    		
    		saveFile.setBounds(130, 40, 240, 20);
    	}
    	return saveFile;
    }
    
    private JButton getSaveBrowseButton() {
    	if (saveFileBrowse == null) {  
    		saveFileBrowse = new JButton("Browse");    		
    		//saveFileBrowse.setBounds(380, 160, 79, 23);
    		saveFileBrowse.setBounds(380, 40, 79, 23);
    		
    		saveFileBrowse.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					BasicFileFilter filter = new BasicFileFilter();
				    filter.addExtension("tree");				    
				    filter.setDescription("Tree Files");
				    				    
					JFileChooser fileChooser = new JFileChooser();
					fileChooser.setFileFilter(filter);
					fileChooser.setCurrentDirectory(new File(dataFilePath));
	                int returnVal = fileChooser.showOpenDialog(TreeGenerationDialog.this);
	                if (returnVal == JFileChooser.APPROVE_OPTION) {
	                	getTextureFile().setText(fileChooser.getSelectedFile().getPath());
	                }
				}
			});
    	}
    	return saveFileBrowse;
    }
       
    private JButton getGenerate() {
    	if (generate == null) {
    		generate = new JButton("Generate");
    		//generate.setBounds(130, 190, 110, 23);
    		generate.setBounds(130, 80, 110, 23);
    		
    		generate.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
//					float baseRadius = Float.valueOf(getBaseRadius().getText());
//					float topRadius = Float.valueOf(getTopRadius().getText());
//					float trunkLength = Float.valueOf(getTrunkLength().getText());
//					Tree.maxAge = Integer.valueOf(getIterations().getText());
					Tree.maxAge = 6;
					String textureFile = getTextureFile().getText();
					TextureLoader texLoader = new TextureLoader();
					if (!textureFile.equals("")) 						
						Tree.branchTexture = texLoader.getTexture(textureFile, true);					
					//Also load coin texture				
					String coinTextureFile = dataFilePath + File.separator + "coins.jpg";
					Tree.coinHeapTexture = texLoader.getTexture(coinTextureFile, true);
										
					parentView.createTrees();
					//parentView.loadTestObject("");
					
					//Save the tree object in a file
					String saveFile = getSaveFile().getText();
//					if (!saveFile.equals("")) parentView.saveTree(saveFile, Tree.maxAge, textureFile, coinTextureFile); 
						
					dispose();
					
				}
			});
    	}
    	return generate;
    }
   
//    private JTextField baseRadius;
//    private JTextField topRadius;
//    private JTextField trunkLength;
//    private JTextField iterations;
    private JTextField textureFile;
    private JButton browse;    
    private JTextField saveFile;
    private JButton saveFileBrowse;
    private JButton generate;  
    
}
