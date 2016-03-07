/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package netdecoder;

import java.awt.FileDialog;
import java.awt.Frame;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;

import org.rosuda.JRI.RMainLoopCallbacks;
import org.rosuda.JRI.Rengine;

/**
 *
 * @author edroaldo
 */

class Console implements RMainLoopCallbacks{
    public void rWriteConsole(Rengine re, String text, int oType) {
        System.out.print(text);
    }
    
    public void rBusy(Rengine re, int which) {
        System.out.println("rBusy("+which+")");
    }
    
    public String rReadConsole(Rengine re, String prompt, int addToHistory) {
        /*System.out.print(prompt);
        try {
            BufferedReader br=new BufferedReader(new InputStreamReader(System.in));
            String s=br.readLine();
            return (s==null||s.length()==0)?s:s+"\n";
        } catch (Exception e) {
            System.out.println("jriReadConsole exception: "+e.getMessage());
        }*/
        return null;
    }
    
    public void rShowMessage(Rengine re, String message) {
        System.out.println("rShowMessage \""+message+"\"");
    }
	
    public String rChooseFile(Rengine re, int newFile) {
	FileDialog fd = new FileDialog(new Frame(), (newFile==0)?"Select a file":"Select a new file", (newFile==0)?FileDialog.LOAD:FileDialog.SAVE);
	fd.show();
	String res=null;
	if (fd.getDirectory()!=null) res=fd.getDirectory();
	if (fd.getFile()!=null) res=(res==null)?fd.getFile():(res+fd.getFile());
	return res;
    }
    
    public void rFlushConsole(Rengine re) {
    }

    public void rLoadHistory(Rengine re, String filename) {
    }

    public void rSaveHistory(Rengine re, String filename) {
    }
}

public class RJava {
    Rengine re;

    /*public RJava() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        //String path = "/usr/local/lib/R/site-library/rJava/jri";
        //addLibraryPath(path);
        System.out.println("java.library.path: " + java.lang.System.getProperty("java.library.path"));
        System.out.println("R_HOME: " + System.getenv("R_HOME"));

        String[] newargs = {"--no-save"};
        re = new Rengine(newargs, true, new Console());
        re.eval("library(gplots);");
        re.eval("library(ggplot2);");
        re.eval("library(grid)");
        re.eval("library(reshape)");
        re.eval("library(reshape2)");
        re.eval("library(plyr)");
        re.eval("library(RColorBrewer);");
        re.eval("library(igraph)");
        re.eval("library(Vennerable)");
    }*/
    public RJava(){
    
    }
    
    
    public void plotDistribution(String filename, String title){
        String file = "\'" + filename + ".txt\'";
        String write_1 = "\'" + filename  + ".pdf" + "\'";
        //System.out.println("saving plots for edge flow distribution....");
        re.eval("df <- read.table(" + file + ", sep='\t', header = TRUE)");
        re.eval("df <- data.frame(condition=df$flows)");
        re.eval("pdf(file=" + write_1 + ", width=2, height=2);");
        re.eval("figure <- ggplot(df, aes(x=condition)) + xlim(0, 1) + geom_histogram(binwidth=0.1, alpha=0.7, colour=\"white\", fill=\"black\") +  xlab(\"Edge flow\") + ylab(\"Number of edges\") + theme_bw() + theme(panel.grid = element_blank(), panel.border=element_blank(), axis.line = element_line(colour = \"black\"), axis.title=element_text(size=10)) + theme(axis.text.x = element_text(size=8, angle=0, hjust=1), axis.text.y = element_text(size=8, hjust=1)) + ggtitle("+"\'"+title+"\'"+")");
        re.eval("plot(figure)");
        re.eval("dev.off()");
    }
   
    public void exportGML(
            String filename_subnet, 
            String filename_totalFlow,
            String filename_differences){
        
        String file_subnet = "\'" + filename_subnet + ".txt\'";
        String file_flowDifference = "\'" + filename_differences + ".txt\'";
        String file_totalFlow = "\'" + filename_totalFlow + ".txt\'";
        String write_1 = "\'" + filename_subnet  + ".gml" + "\'";
        //System.out.println("exporting .GML files...");
        
        re.eval("subnet <- read.table(" + file_subnet + ", sep='\t', header = TRUE)");
        re.eval("data <- read.table(" + file_flowDifference + ", sep='\t', header = TRUE)");
        re.eval("totalFlow <- read.table(" + file_totalFlow + ", sep='\t', header = TRUE)");
        re.eval("colnames(subnet) <- c('from', 'to', 'weight', 'correlation', 'sign')");
        re.eval("iG <- graph.data.frame(subnet, directed=TRUE)");
        re.eval("V(iG)$label <- V(iG)$name");
        //re.eval("xcols<-bluered(length(data$difference));");
        //re.eval("xcols <- colfunc<-colorRampPalette(c(\"white\", \"goldenrod1\", \"red\"))");
        //re.eval("xcols <- colfunc<-colorRampPalette(c(\"white\", \"darkorange\", \"red\"))");
        re.eval("xcols <- colfunc<-colorRampPalette(c(\"blue\", \"white\", \"red\"))");
        //re.eval("xcols <- colfunc<-colorRampPalette(c(\"white\", \"red\"))");
        re.eval("xcols <- xcols(length(data$flows))");
        re.eval("mycols<-rep('', length(data$flows));");
        re.eval("mycols[order(data$flows)]<-xcols;");
        re.eval("V(iG)[as.vector(data$gene)]$colors <- mycols");
        re.eval("V(iG)[as.vector(data$gene)]$diffs <- data$flows");
        re.eval("V(iG)[as.vector(totalFlow$gene)]$totalFlows <- totalFlow$flows");
        re.eval("V(iG)$degree <- igraph::degree(iG)"); //replace by total flow...
        re.eval("neighS <- V(induced.subgraph(graph=iG,vids=unlist(neighborhood(graph=iG,order=1,nodes='s'))))$name");
        re.eval("neighT <- V(induced.subgraph(graph=iG,vids=unlist(neighborhood(graph=iG,order=1,nodes='t'))))$name");
        re.eval("V(iG)$props = rep(\"PROTEIN\", length(V(iG)$name))");
        re.eval("V(iG)[match(neighS, V(iG)$name)]$props = \"SOURCE\";");
        re.eval("V(iG)[match(neighT, V(iG)$name)]$props = \"TARGET\";");
        re.eval("iG2 <- delete.vertices(iG, c('s','t'))");
        re.eval("write.graph(iG2,"+write_1+ ", format = 'gml')");
    }
    
    public void exportGML2(
            String filename_subnet, 
            String filename_totalFlow,
            String filename_differences){
        
        String file_subnet = "\'" + filename_subnet + ".txt\'";
        String file_flowDifference = "\'" + filename_differences + ".txt\'";
        String file_totalFlow = "\'" + filename_totalFlow + ".txt\'";
        String write_1 = "\'" + filename_subnet  + ".gml" + "\'";
        //System.out.println("exporting .GML files...");
        
        re.eval("subnet <- read.table(" + file_subnet + ", sep='\t', header = TRUE)");
        re.eval("data <- read.table(" + file_flowDifference + ", sep='\t', header = TRUE)");
        re.eval("totalFlow <- read.table(" + file_totalFlow + ", sep='\t', header = TRUE)");
        re.eval("colnames(subnet) <- c('from', 'to', 'weight', 'correlation', 'sign')");
        re.eval("iG <- graph.data.frame(subnet, directed=TRUE)");
        re.eval("V(iG)$label <- V(iG)$name");
        re.eval("xcols <- colfunc<-colorRampPalette(c(\"blue\", \"white\", \"red\"))");
        re.eval("xcols <- xcols(length(data$flows))");
        re.eval("mycols<-rep('', length(data$flows));");
        re.eval("mycols[order(data$flows)]<-xcols;");
        re.eval("V(iG)[as.vector(data$gene)]$colors <- mycols");
        re.eval("V(iG)[as.vector(data$gene)]$diffs <- data$flows");
        re.eval("V(iG)[as.vector(totalFlow$gene)]$totalFlows <- totalFlow$flows");
        re.eval("V(iG)$degree <- igraph::degree(iG)"); //replace by total flow...
        re.eval("write.graph(iG,"+write_1+ ", format = 'gml')");
    }
    
    
    /*public void exportGML(String dir, String filename_subnet, String filename_ratios){
        String file_subnet = "\'" + dir + filename_subnet + ".txt\'";
        String file_flowDifference = "\'" + dir + filename_ratios + ".txt\'";
        String write_1 = "\'" + dir + filename_subnet  + ".gml" + "\'";
        System.out.println("output dir:" + file_subnet);
        
        re.eval("subnet <- read.table(" + file_subnet + ", sep='\t', header = TRUE)");
        re.eval("ratios <- read.table(" + file_flowDifference + ", sep='\t', header = TRUE)");
        re.eval("colnames(subnet) <- c('from', 'to', 'weight')");
        re.eval("iG <- graph.data.frame(subnet, directed=TRUE)");
        re.eval("V(iG)$label <- V(iG)$name");
        re.eval("xcols<-bluered(length(ratios$ratio));");
        re.eval("mycols<-rep('', length(ratios$ratio));");
        re.eval("mycols[order(ratios$ratio)]<-xcols;");
        re.eval("V(iG)[as.vector(ratios$gene)]$colors <- mycols");
        re.eval("V(iG)[c('s','t')]$colors <- '#66FF66'");
        re.eval("write.graph(iG,"+write_1+ ", format = 'gml')");
    }*/
    
    public void plotHeatmapSH(String name, String disease,
            int threshold, String type,  String filename){
        String file = "\'" + name + ".txt\'";
        String write_1 = "\'" + filename + "_" + type + ".pdf" + "\'";
        String write_2 = "\'" + filename + "_" + type + "_TOP.txt" + "\'";
        //System.out.println(file);
        System.out.println("saving heatmaps...");
        String title = type;
        re.eval("data <- read.table(" + file + ", sep='\t', header = TRUE)");
        re.eval("rownames(data) <- as.vector(data$X)");
        re.eval("data <- data[,c(2, 3)]");
        re.eval("colnames(data) <- c('control'," + "\'"+ disease + "\'" +")");
        re.eval("data <- log2(data + 1)");
        re.eval("data <- data[order(data$"+disease+", decreasing=TRUE),]");
        re.eval("top<-" + threshold);
        re.eval("data <- data[1:top,]");
        re.eval("aux <- data");
        re.eval("aux$gene <- rownames(aux)");
        re.eval("aux <- aux[,c('gene', 'control', "+ "\'" + disease + "\'" +")]");
        re.eval("write.table(aux, file=" + write_2 + ", row.names=FALSE, col.names=TRUE, sep='\t')");
        re.eval("df <- data");
        re.eval("df$genes <- rownames(df)");
        re.eval("matrix.m <- melt(df, id=c(\"genes\"))");
        re.eval("matrix.m$genes <- factor(matrix.m$genes, levels=matrix.m[order(matrix.m$value, decreasing=TRUE), \"genes\"])");
        re.eval("range <- range(matrix.m$value)");
        //re.eval("pdf(file=" + write_1 + ", width=4, height=2.3);");
        //re.eval("figure <- ggplot(matrix.m, aes(variable, genes)) + geom_tile(aes(fill = value), colour = \"black\") + scale_fill_gradient2(\"log2(total flow)\", limits=range, low=\"white\", high=\"red\", guide=\"colorbar\") + theme_bw() + xlab(\"\") + ylab(\"\") + theme(panel.grid=element_blank(), panel.border=element_blank(),legend.position=\"top\", legend.direction=\"horizontal\") + theme(axis.text.x = element_text(size=rel(0.75), angle=45, hjust=1), axis.text.y = element_text(size = rel(0.85), angle = 00))  + coord_flip() + ggtitle("+"\'"+title+"\'"+")");
        re.eval("pdf(file=" + write_1 + ", width=2.5, height=3.5);");
        re.eval("figure <- ggplot(matrix.m, aes(variable, genes)) + geom_tile(aes(fill = value), colour = \"black\") + scale_fill_gradient2(\"log2\n(total flow)\", limits=range, low=\"white\", high=\"red\", guide=\"colorbar\") + theme_bw() + xlab(\"\") + ylab(\"\") + theme(legend.key.size = unit(0.3, \"cm\"), panel.grid=element_blank(), panel.border=element_blank(),legend.position=\"right\", legend.direction=\"vertical\") + theme(axis.text.x = element_text(size=rel(0.75), angle=45, hjust=1), axis.text.y = element_text(size = rel(0.75), angle = 00)) + ggtitle("+"\'"+title+"\'"+")");
        
        re.eval("print(figure)");
        re.eval("dev.off()");
        
    }
    
    public void plotHeatmapSH2(String name, String disease,
            int threshold, String type,  String filename){
        String file = "\'" + name + ".txt\'";
        String write_1 = "\'" + filename + "_" + type + ".pdf" + "\'";
        //System.out.println(file);
        System.out.println("saving heatmaps...");
        String title = type;
        re.eval("df <- read.table(" + file + ", sep='\t', header = TRUE)");
        re.eval("matrix.m <- melt(df, id=c('gene'))");
        re.eval("matrix.m$gene <- factor(matrix.m$gene, levels=matrix.m[order(matrix.m$value, decreasing=FALSE), 'gene'])");
        re.eval("range <- range(matrix.m$value)");
        re.eval("pdf(file=" + write_1 + ", width=2.3, height=3.5);");
        re.eval("figure <- ggplot(matrix.m, aes(variable, gene)) + geom_tile(aes(fill = value), colour = \"black\") + scale_fill_gradient2(\"flow\ndifference\", limits=range, low=\"blue\", high=\"red\", guide=\"colorbar\") + theme_bw() + xlab(\"\") + ylab(\"\") + theme(legend.key.size = unit(0.3, \"cm\"), panel.grid=element_blank(), panel.border=element_blank(),legend.position=\"right\", legend.direction=\"vertical\") + theme(axis.text.x = element_text(size=rel(0.75), angle=45, hjust=1), axis.text.y = element_text(size = rel(0.75), angle = 00)) + ggtitle("+"\'"+title+"\'"+")");
        re.eval("print(figure)");
        re.eval("dev.off()");
        
    }
    
    public void plotVennImpactScore(
            String dir, 
            String nameControl, 
            String nameDisease,
            String condition,
            String filename){
        
        String fileControl = "\'" + dir + nameControl + "\'";
        String fileDisease = "\'" + dir + nameDisease + ".txt\'";
        String write_1 = "\'" + dir + filename  + ".pdf" + "\'";
        System.out.println("saving Venn diagrams...");
        
        re.eval("control <- read.table(" + fileControl + ", sep='\t', header = TRUE)");
        re.eval("disease <- read.table(" + fileDisease + ", sep='\t', header = TRUE)");
        re.eval("colnames(disease) <- c('expression', 'genes', 'CSS')");
        re.eval("venn <- list()");
        re.eval("venn[['candidate genes']] <- as.vector(control$genes)");
        re.eval("venn[["+ "\'" + condition +"\'"+"]] <- as.vector(disease$genes)");
        re.eval("venTF <- Venn(venn);");
        re.eval("pdf(file=" + write_1 + ", width=3, height=3)");
        re.eval("plot(venTF,doWeights=FALSE)");
        re.eval("dev.off()");
    }
    
    public void plotVennGenesPaths(
            String dir, 
            String nameControl, 
            String nameDisease,
            String condition,
            String type, //gene or path
            String filename){
        
        String fileControl = "\'" + nameControl + ".txt\'";
        String fileDisease = "\'" + nameDisease + ".txt\'";
        String write_1 = "\'" + dir + filename  + ".pdf" + "\'";
        System.out.println("saving Venn diagrams...");
        
        
        re.eval("control <- read.table(" + fileControl + ", sep='\t', header = TRUE)");
        re.eval("disease <- read.table(" + fileDisease + ", sep='\t', header = TRUE)");
        re.eval("venn <- list()");
        re.eval("venn[['control']] <- as.vector(control$"+type+")");
        re.eval("venn[["+ "\'" + condition +"\'"+"]] <- as.vector(disease$"+type+")");
        re.eval("venTF <- Venn(venn);");
        re.eval("pdf(file=" + write_1 + ", width=3, height=3)");
        re.eval("plot(venTF,doWeights=FALSE)");
        re.eval("dev.off()");
    }
    
    public void plotVennEdges(String dir, 
            String nameControl, 
            String nameDisease,
            String condition,
            String filename){
        String fileControl = "\'" + nameControl + ".txt\'";
        String fileDisease = "\'" + nameDisease + ".txt\'";
        String write_1 = "\'" + dir + filename  + ".pdf" + "\'";
        System.out.println("saving Venn diagrams...");
        
        re.eval("control <- read.table(" + fileControl + ", sep='\t', header = TRUE)");
        re.eval("disease <- read.table(" + fileDisease + ", sep='\t', header = TRUE)");
        re.eval("control$edge <- paste(control$from, control$to, sep='->')");
        re.eval("disease$edge <- paste(disease$from, disease$to, sep='->')");
        re.eval("venn <- list()");
        re.eval("venn[['control']] <- as.vector(control$edge)");
        re.eval("venn[["+ "\'" + condition +"\'"+"]] <- as.vector(disease$edge)");
        re.eval("venTF <- Venn(venn);");
        re.eval("pdf(file=" + write_1 + ", width=3, height=3)");
        re.eval("plot(venTF,doWeights=FALSE)");
        re.eval("dev.off()");
    
    }

    public String createDistributionScript(String filename, String title){
        String scriptName = filename+"_distribution.R";
        String file = "\'" + filename + ".txt\'";
        String write_1 = "\'" + filename + ".pdf" + "\'";
        //System.out.println("saving plots for edge flow distribution....");

        try {
            File f = new File(scriptName);
            if (!f.exists()) {
                f.createNewFile();
            }
            FileWriter fw = new FileWriter(f.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write("library(gplots);\n");
            bw.write("library(ggplot2);\n");
            bw.write("library(grid); \n");
            bw.write("library(reshape); \n");
            bw.write("library(reshape2); \n");
            bw.write("library(plyr); \n");
            bw.write("library(RColorBrewer); \n");
            bw.write("library(igraph); \n");
            bw.write("library(Vennerable); \n");

            bw.write("df <- read.table(" + file + ", sep='\t', header = TRUE); \n");
            bw.write("df <- data.frame(condition=df$flows); \n");
            bw.write("pdf(file=" + write_1 + ", width=2, height=2); \n");
            bw.write("figure <- ggplot(df, aes(x=condition)) + xlim(0, 1) + geom_histogram(binwidth=0.1, alpha=0.7, colour=\"white\", fill=\"black\") +  xlab(\"Edge flow\") + ylab(\"Number of edges\") + theme_bw() + theme(panel.grid = element_blank(), panel.border=element_blank(), axis.line = element_line(colour = \"black\"), axis.title=element_text(size=10)) + theme(axis.text.x = element_text(size=8, angle=0, hjust=1), axis.text.y = element_text(size=8, hjust=1)) + ggtitle(" + "\'" + title + "\'" + "); \n");
            bw.write("plot(figure); \n");
            bw.write("dev.off(); \n");
            bw.write("#--------------------------------------------------------------------------\n");
            bw.close();
        } catch (IOException io) {
            System.out.println(io.getMessage());
        }
        /*try {
            Runtime.getRuntime().exec("R --slave --no-save CMD BATCH " + scriptName);
        } catch (IOException io) {
            System.out.println(io.getMessage());
        }*/
        return scriptName;
    }
    
    public String createScriptExportGML2(
            String filename_subnet, 
            String filename_totalFlow,
            String filename_differences){
        
        String scriptName = filename_subnet+".R";
        String file_subnet = "\'" + filename_subnet + ".txt\'";
        String file_flowDifference = "\'" + filename_differences + ".txt\'";
        String file_totalFlow = "\'" + filename_totalFlow + ".txt\'";
        String write_1 = "\'" + filename_subnet + ".gml" + "\'";
        //System.out.println("exporting .GML files...");

        try {
            File f = new File(scriptName);
            if (!f.exists()) {
                f.createNewFile();
            }
            FileWriter fw = new FileWriter(f.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write("library(gplots);\n");
            bw.write("library(ggplot2);\n");
            bw.write("library(grid); \n");
            bw.write("library(reshape); \n");
            bw.write("library(reshape2); \n");
            bw.write("library(plyr); \n");
            bw.write("library(RColorBrewer); \n");
            bw.write("library(igraph); \n");
            bw.write("library(Vennerable); \n");

            bw.write("subnet <- read.table(" + file_subnet + ", sep='\t', header = TRUE); \n");
            bw.write("data <- read.table(" + file_flowDifference + ", sep='\t', header = TRUE); \n");
            bw.write("totalFlow <- read.table(" + file_totalFlow + ", sep='\t', header = TRUE); \n");
            bw.write("colnames(subnet) <- c('from', 'to', 'weight', 'correlation', 'sign'); \n");
            bw.write("iG <- graph.data.frame(subnet, directed=TRUE); \n");
            bw.write("V(iG)$label <- V(iG)$name; \n");
            bw.write("xcols <- colfunc<-colorRampPalette(c(\"blue\", \"white\", \"red\")); \n");
            bw.write("xcols <- xcols(length(data$flows)); \n");
            bw.write("mycols<-rep('', length(data$flows)); \n");
            bw.write("mycols[order(data$flows)]<-xcols; \n");
            bw.write("V(iG)[as.vector(data$gene)]$colors <- mycols; \n");
            bw.write("V(iG)[as.vector(data$gene)]$diffs <- data$flows; \n");
            bw.write("V(iG)[as.vector(totalFlow$gene)]$totalFlows <- totalFlow$flows; \n");
            bw.write("V(iG)$degree <- igraph::degree(iG); \n"); //replace by total flow...
            bw.write("write.graph(iG," + write_1 + ", format = 'gml'); \n");
            bw.write("#--------------------------------------------------------------------------\n");
            bw.close();
        } catch (IOException io) {
            System.out.println(io.getMessage());
        }
        /*try {
            Runtime.getRuntime().exec("R --slave --no-save CMD BATCH " + scriptName);
        } catch (IOException io) {
            System.out.println(io.getMessage());
        }*/
        return scriptName;
    }
    
    public String createHeatmapSH2Script(String name, String disease,
            int threshold, String type,  String filename){
        
        String scriptName = filename+ "_"+type+"_heatmapSH.R";
        String file = "\'" + name + ".txt\'";
        String write_1 = "\'" + filename + "_" + type + ".pdf" + "\'";
        //System.out.println(file);
        System.out.println("saving heatmaps...");
        String title = type;
        try {
            File f = new File(scriptName);
            if (!f.exists()) {
                f.createNewFile();
            }
            FileWriter fw = new FileWriter(f.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write("library(gplots);\n");
            bw.write("library(ggplot2);\n");
            bw.write("library(grid); \n");
            bw.write("library(reshape); \n");
            bw.write("library(reshape2); \n");
            bw.write("library(plyr); \n");
            bw.write("library(RColorBrewer); \n");
            bw.write("library(igraph); \n");
            bw.write("library(Vennerable); \n");

            bw.write("df <- read.table(" + file + ", sep='\t', header = TRUE); \n");
            bw.write("matrix.m <- melt(df, id=c('gene')); \n");
            bw.write("matrix.m$gene <- factor(matrix.m$gene, levels=matrix.m[order(matrix.m$value, decreasing=FALSE), 'gene']); \n");
            bw.write("range <- range(matrix.m$value); \n");
            bw.write("pdf(file=" + write_1 + ", width=2.3, height=3.5);\n");
            bw.write("figure <- ggplot(matrix.m, aes(variable, gene)) + geom_tile(aes(fill = value), colour = \"black\") + scale_fill_gradient2(\"flow\ndifference\", limits=range, low=\"blue\", high=\"red\", guide=\"colorbar\") + theme_bw() + xlab(\"\") + ylab(\"\") + theme(legend.key.size = unit(0.3, \"cm\"), panel.grid=element_blank(), panel.border=element_blank(),legend.position=\"right\", legend.direction=\"vertical\") + theme(axis.text.x = element_text(size=rel(0.75), angle=45, hjust=1), axis.text.y = element_text(size = rel(0.75), angle = 00)) + ggtitle(" + "\'" + title + "\'" + "); \n");
            bw.write("print(figure); \n");
            bw.write("dev.off(); \n");
            bw.write("#--------------------------------------------------------------------------\n");
            bw.close();
        } catch (IOException io) {
            System.out.println(io.getMessage());
        }
        /*try {
            Runtime.getRuntime().exec("R --slave --no-save CMD BATCH " + scriptName);
        } catch (IOException io) {
            System.out.println(io.getMessage());
        }*/
        return scriptName;
    }
    
    
    public String createJaccardMatrixScript(String filename) {
        String scriptName = filename+"_jaccardMatrix.R";
        String file = "\'" + filename + ".txt" + "\'";
        String write = "\'" + filename + ".pdf" + "\'";

        System.out.println("saving Jaccard index heatmap...");
        try {
            File f = new File(scriptName);
            if (!f.exists()) {
                f.createNewFile();
            }
            FileWriter fw = new FileWriter(f.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write("library(gplots);\n");
            bw.write("library(ggplot2);\n");
            bw.write("library(grid); \n");
            bw.write("library(reshape); \n");
            bw.write("library(reshape2); \n");
            bw.write("library(plyr); \n");
            bw.write("library(RColorBrewer); \n");
            bw.write("library(igraph); \n");
            bw.write("library(Vennerable); \n");

            bw.write("sim <- read.csv(" + file + ", sep='\t'); \n");
            bw.write("names <- as.vector(sim$X); \n");
            bw.write("sim <- sim[,2:dim(sim)[2]]; \n");
            bw.write("rownames(sim) <- names; \n");
            bw.write("hmcols<- colorRampPalette(c(\"black\", \"yellow\")); \n");
            bw.write("pdf(file=" + write + ", width=5, height=5); \n");
            bw.write("heatmap.2(as.matrix(t(sim)), col=hmcols,trace=\"none\", density.info=\"none\", scale=\"none\",margin=c(15,15), key=TRUE, Rowv=T, Colv=T,srtCol=60, dendrogram=\"none\", cexCol=0.55, cexRow=0.55, symm=T,  sepcolor='black'); \n");
            bw.write("dev.off(); \n");
            bw.write("#--------------------------------------------------------------------------\n");
            bw.close();
        } catch (IOException io) {
            System.out.println(io.getMessage());
        }
        /*try {
            Runtime.getRuntime().exec("R --slave --no-save CMD BATCH " + scriptName);
        } catch (IOException io) {
            System.out.println(io.getMessage());
        }*/
        return scriptName;
    }
    
    public String createAdjAdjMatrixScript(String dir, String filename, String gene) {
        
        String scriptName = dir+filename+"_adjMatrix.R";
        String file = "\'" + dir + filename + ".txt" + "\'";
        String write = "\'" + dir + filename + ".pdf" + "\'";
        System.out.println("saving adjacency matrices...");
        
        try {
            File f = new File(scriptName);
            if (!f.exists()) {
                f.createNewFile();
            }
            FileWriter fw = new FileWriter(f.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write("library(gplots);\n");
            bw.write("library(ggplot2);\n");
            bw.write("library(grid); \n");
            bw.write("library(reshape); \n");
            bw.write("library(reshape2); \n");
            bw.write("library(plyr); \n");
            bw.write("library(RColorBrewer); \n");
            bw.write("library(igraph); \n");
            bw.write("library(Vennerable); \n");

            bw.write("matrix <- read.csv(" + file + ", sep='\t'); \n");
            bw.write("rownames(matrix) <- matrix$gene; \n");
            bw.write("matrix <- matrix[, 2:dim(matrix)[2]]; \n");
            bw.write("matrix[row(matrix) > col(matrix)] <- 0.5; \n");
            bw.write("diag(matrix) <- 0.5; \n");
            //bw.write("matrix[lower.tri(matrix)]<- NA; \n");
            bw.write("matrix <- data.frame(gene=rownames(matrix), matrix); \n");
            bw.write("matrix.m <- melt(matrix, id.var='gene'); \n");
            bw.write("matrix.m <- na.omit(matrix.m); \n");
            bw.write("matrix.m$gene <- factor(matrix.m$gene, levels=matrix.m$gene); \n");
            bw.write("matrix.m$variable <- factor(matrix.m$variable, levels=rev(levels(matrix.m$gene))); \n");
            bw.write("matrix.m <- ddply(matrix.m, .(variable), transform, rescale = value); \n");
            bw.write("w=dim(matrix)[1] / 2.5; \n");
            bw.write("h=dim(matrix)[2] / 3; \n");
            bw.write("pdf(file=" + write + ", width=w, height=h); \n");
            bw.write("figure <- ggplot(matrix.m, aes(gene, variable)) + geom_tile(aes(fill = value), colour = 'black') + scale_fill_gradient(low = 'white', high = 'red') + theme_bw() + xlab('') + ylab('') + theme(panel.grid=element_blank(), panel.border=element_blank(), legend.position='none', axis.text.x = element_text(size=rel(1), angle=45, hjust=1),axis.title.y = element_text(size = rel(0.3), angle = 90)) + ggtitle(" + "\'" + gene + "\'" + "); \n");
            bw.write("print(figure); \n");
            //bw.write("ggsave(filename='/home/edroaldo/NetDecoder_Tests/seilaTESTE.pdf', plot=figure); \n");
            bw.write("dev.off(); \n");
            bw.write("#--------------------------------------------------------------------------\n");
            bw.close();
        } catch (IOException io) {
            System.out.println(io.getMessage());
        }
        /*try {
            Runtime.getRuntime().exec("R --slave --no-save CMD BATCH " + scriptName);
        } catch (IOException io) {
            System.out.println(io.getMessage());
        }*/
        return scriptName;
    }
    
    public String createVennEdgesScript(String dir, 
            String nameControl, 
            String nameDisease,
            String condition,
            String filename){
        
        String scriptName = dir+filename+"_vennDiagramsEdges.R";
        String fileControl = "\'" + nameControl + ".txt\'";
        String fileDisease = "\'" + nameDisease + ".txt\'";
        String write_1 = "\'" + dir + filename + ".pdf" + "\'";
        System.out.println("saving Venn diagrams...");

        try {
            File f = new File(scriptName);
            if (!f.exists()) {
                f.createNewFile();
            }
            FileWriter fw = new FileWriter(f.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write("library(gplots);\n");
            bw.write("library(ggplot2);\n");
            bw.write("library(grid); \n");
            bw.write("library(reshape); \n");
            bw.write("library(reshape2); \n");
            bw.write("library(plyr); \n");
            bw.write("library(RColorBrewer); \n");
            bw.write("library(igraph); \n");
            bw.write("library(Vennerable); \n");

            bw.write("control <- read.table(" + fileControl + ", sep='\t', header = TRUE); \n");
            bw.write("disease <- read.table(" + fileDisease + ", sep='\t', header = TRUE); \n");
            bw.write("control$edge <- paste(control$from, control$to, sep='->'); \n");
            bw.write("disease$edge <- paste(disease$from, disease$to, sep='->'); \n");
            bw.write("venn <- list(); \n");
            bw.write("venn[['control']] <- as.vector(control$edge); \n");
            bw.write("venn[[" + "\'" + condition + "\'" + "]] <- as.vector(disease$edge); \n");
            bw.write("venTF <- Venn(venn); \n");
            bw.write("pdf(file=" + write_1 + ", width=3, height=3); \n");
            bw.write("plot(venTF,doWeights=FALSE); \n");
            bw.write("dev.off(); \n");
            bw.write("#--------------------------------------------------------------------------\n");
            bw.close();
        } catch (IOException io) {
            System.out.println(io.getMessage());
        }
        /*try {
            Runtime.getRuntime().exec("R --slave --no-save CMD BATCH " + scriptName);
        } catch (IOException io) {
            System.out.println(io.getMessage());
        }*/
        return scriptName;
    }
    
    
    public String createVennGenesPathsScript(
            String dir, 
            String nameControl, 
            String nameDisease,
            String condition,
            String type, //gene or path
            String filename){
        
        String scriptName = dir+filename+"_vennDiagramsGenesPaths.R";
        String fileControl = "\'" + nameControl + ".txt\'";
        String fileDisease = "\'" + nameDisease + ".txt\'";
        String write_1 = "\'" + dir + filename  + ".pdf" + "\'";
        System.out.println("saving Venn diagrams...");
        
        try {
            File f = new File(scriptName);
            if (!f.exists()) {
                f.createNewFile();
            }
            FileWriter fw = new FileWriter(f.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write("library(gplots);\n");
            bw.write("library(ggplot2);\n");
            bw.write("library(grid); \n");
            bw.write("library(reshape); \n");
            bw.write("library(reshape2); \n");
            bw.write("library(plyr); \n");
            bw.write("library(RColorBrewer); \n");
            bw.write("library(igraph); \n");
            bw.write("library(Vennerable); \n");
            bw.write("control <- read.table(" + fileControl + ", sep='\t', header = TRUE); \n");
            bw.write("disease <- read.table(" + fileDisease + ", sep='\t', header = TRUE); \n");
            bw.write("venn <- list(); \n");
            bw.write("venn[['control']] <- as.vector(control$" + type + "); \n");
            bw.write("venn[[" + "\'" + condition + "\'" + "]] <- as.vector(disease$" + type + "); \n");
            bw.write("venTF <- Venn(venn); \n");
            bw.write("pdf(file=" + write_1 + ", width=3, height=3); \n");
            bw.write("plot(venTF,doWeights=FALSE); \n");
            bw.write("dev.off(); \n");
            bw.write("#--------------------------------------------------------------------------\n");
            bw.close();
        } catch (IOException io) {
            System.out.println(io.getMessage());
        }
        /*try {
            Runtime.getRuntime().exec("R --slave --no-save CMD BATCH " + scriptName);
        } catch (IOException io) {
            System.out.println(io.getMessage());
        }*/
        return scriptName;
    }

    public String createScriptExportGML(
            String filename_subnet,
            String filename_totalFlow,
            String filename_differences) {

        String scriptName = filename_subnet + "_exportGML1.R";
        String file_subnet = "\'" + filename_subnet + ".txt\'";
        String file_flowDifference = "\'" + filename_differences + ".txt\'";
        String file_totalFlow = "\'" + filename_totalFlow + ".txt\'";
        String write_1 = "\'" + filename_subnet + ".gml" + "\'";
        //System.out.println("exporting .GML files...");
       try {
            File f = new File(scriptName);
            if (!f.exists()) {
                f.createNewFile();
            }
            FileWriter fw = new FileWriter(f.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write("library(gplots);\n");
            bw.write("library(ggplot2);\n");
            bw.write("library(grid); \n");
            bw.write("library(reshape); \n");
            bw.write("library(reshape2); \n");
            bw.write("library(plyr); \n");
            bw.write("library(RColorBrewer); \n");
            bw.write("library(igraph); \n");
            bw.write("library(Vennerable); \n");
            
            bw.write("subnet <- read.table(" + file_subnet + ", sep='\t', header = TRUE);\n");
            bw.write("data <- read.table(" + file_flowDifference + ", sep='\t', header = TRUE);\n");
            bw.write("totalFlow <- read.table(" + file_totalFlow + ", sep='\t', header = TRUE);\n");
            bw.write("colnames(subnet) <- c('from', 'to', 'weight', 'correlation', 'sign');\n");
            bw.write("iG <- graph.data.frame(subnet, directed=TRUE);\n");
            bw.write("V(iG)$label <- V(iG)$name;\n");
            //bw.write("xcols<-bluered(length(data$difference));\n");
            //bw.write("xcols <- colfunc<-colorRampPalette(c(\"white\", \"goldenrod1\", \"red\"));\n");
            //bw.write("xcols <- colfunc<-colorRampPalette(c(\"white\", \"darkorange\", \"red\"));\n");
            bw.write("xcols <- colfunc<-colorRampPalette(c(\"blue\", \"white\", \"red\"));\n");
            bw.write("xcols <- colfunc<-colorRampPalette(c(\"white\", \"red\"));\n");
            bw.write("xcols <- xcols(length(data$flows));\n");
            bw.write("mycols<-rep('', length(data$flows));\n");
            bw.write("mycols[order(data$flows)]<-xcols;\n");
            bw.write("V(iG)[as.vector(data$gene)]$colors <- mycols;\n");
            bw.write("V(iG)[as.vector(data$gene)]$diffs <- data$flows;\n");
            bw.write("V(iG)[as.vector(totalFlow$gene)]$totalFlows <- totalFlow$flows;\n");
            bw.write("V(iG)$degree <- igraph::degree(iG);\n"); //replace by total flow...
            bw.write("neighS <- V(induced.subgraph(graph=iG,vids=unlist(neighborhood(graph=iG,order=1,nodes='s'))))$name;\n");
            bw.write("neighT <- V(induced.subgraph(graph=iG,vids=unlist(neighborhood(graph=iG,order=1,nodes='t'))))$name;\n");
            bw.write("V(iG)$props = rep(\"PROTEIN\", length(V(iG)$name));\n");
            bw.write("V(iG)[match(neighS, V(iG)$name)]$props = \"SOURCE\";\n");
            bw.write("V(iG)[match(neighT, V(iG)$name)]$props = \"TARGET\";\n");
            bw.write("iG2 <- delete.vertices(iG, c('s','t'));\n");
            bw.write("write.graph(iG2," + write_1 + ", format = 'gml');\n");
            bw.write("#--------------------------------------------------------------------------\n");
            bw.close();
        } catch (IOException io) {
            System.out.println(io.getMessage());
        }
        /*try {
            Runtime.getRuntime().exec("R --slave --no-save CMD BATCH " + scriptName);
        } catch (IOException io) {
            System.out.println(io.getMessage());
        }*/
       return scriptName;
    }
    
    public String createBarplotScript(String dir, String name, String disease, 
            double corThreshold, double ratioThreshold, String filename){
        String scriptName = dir + filename + "_barplotScript.R";
        String file = "\'" + name + ".txt\'";
        String write_1 = "\'" + dir + filename  + "_keyEdges.pdf" + "\'";
        String write_2 = "\'" + dir + filename  + "_keyEdges.txt" + "\'";
        
        try {
            File f = new File(scriptName);
            if (!f.exists()) {
                f.createNewFile();
            }
            FileWriter fw = new FileWriter(f.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write("library(gplots);\n");
            bw.write("library(ggplot2);\n");
            bw.write("library(grid); \n");
            bw.write("library(reshape); \n");
            bw.write("library(reshape2); \n");
            bw.write("library(plyr); \n");
            bw.write("library(RColorBrewer); \n");
            bw.write("library(igraph); \n");
            bw.write("library(Vennerable); \n");
            bw.write("flows <- read.table(" + file + ", sep='\t', header = TRUE); \n");
            bw.write("rownames(flows) <- as.vector(flows$edge); \n");
            bw.write("flows <- flows[,c(2, 3)]; \n");
            bw.write("flows <- flows[which(flows$" + disease + " > " + corThreshold + "),]; \n");
            bw.write("flows <- flows[which(flows$" + disease + " / flows$control > " + ratioThreshold + "),]; \n");
            bw.write("flows <- flows[order(flows$" + disease + ", decreasing=TRUE),]; \n");
            bw.write("pca <- prcomp(t(flows), center=TRUE, scale=FALSE); \n");
            bw.write("max <- 10; \n");
            bw.write("loadings <- pca$rotation; \n");
            bw.write("pc1 <- loadings[order(loadings[,1], decreasing=TRUE), 1]; \n");
            bw.write("pc2 <- loadings[order(loadings[,2], decreasing=TRUE), 2]; \n");
            bw.write("top.genes <- unique(c(names(pc1[1:max]), names(pc1[(length(pc1) - max):length(pc1)]), names(pc2[1:max]), names(pc2[(length(pc2) - max):length(pc2)]))); \n");
            bw.write("flows.m <- flows[top.genes,]; \n");
            bw.write("write.table(flows.m, file=" + write_2 + ", sep='\t', quote=FALSE); \n");
            bw.write("flows.m <- flows.m[order(flows.m$control, decreasing=TRUE),]; \n");
            //bw.write("flows.m <- flows.m[order(flows.m$"+disease+", decreasing=TRUE),]; \n");
            bw.write("flows.m$edges = rownames(flows.m); \n");
            bw.write("flows.m <- melt(flows.m, id.var='edges'); \n");
            bw.write("flows.m$edges <- factor(flows.m$edges, levels=flows.m$edges); \n");
            bw.write("colls <- c('#BDC9E1', '#67A9CF', '#1C9099', '#016C59', '#0023a0', '#f9a635', 'red', 'black'); \n");
            bw.write("pdf(file=" + write_1 + ", width=5, height=3); \n");
            //bw.write("pdf(file=" + write_1 + ", width=2.5, height=5); \n");
            bw.write("figure <- ggplot(data=flows.m, aes(x=edges, y=value, group=variable, fill=variable))+ geom_bar(stat=\"identity\", width=.7, colour='black') + theme_bw() + guides(fill=guide_legend(ncol=2)) + theme(legend.position=\"top\",legend.key.size = unit(0.3, \"cm\"), legend.text=element_text(size=7)) + xlab(\"\") + ylab(\"Edge Flow\") + theme(axis.text.x = element_text(size=6, angle=45, hjust=1), axis.title.y = element_text(size = rel(1), angle = 90), axis.text.y = element_text(size=6, angle=00, hjust=1)) + scale_fill_manual('', values=c(colls[3], colls[6])); \n");
            //re.eval("figure <- ggplot(data=flows.m, aes(x=edges, y=value, group=variable, fill=variable))+ geom_bar(stat=\"identity\", width=.7, colour='black') + theme_bw() + guides(fill=guide_legend(ncol=2)) + theme(legend.position=\"top\",legend.key.size = unit(0.3, \"cm\"), legend.text=element_text(size=7)) + xlab(\"\") + ylab(\"Edge Flow\") + theme(axis.text.x = element_text(size=6, angle=00, hjust=1), axis.title.y = element_text(size = rel(1), angle = 00), axis.text.y = element_text(size=6, angle=00, hjust=1)) + scale_fill_manual('', values=c(colls[3], colls[6])) + coord_flip(); \n");
            bw.write("print(figure); \n");
            bw.write("dev.off(); \n");
            bw.write("#--------------------------------------------------------------------------\n");
            bw.close();
            
        } catch (IOException io) {
            System.out.println(io.getMessage());
        }
        try{
            System.out.println("Running Runtime.exec() to create barplot");
            Runtime.getRuntime().exec("R --slave --no-save CMD BATCH " +scriptName);
            
        }catch(IOException io){
            System.out.println(io.getMessage());
        }
        return scriptName;
    }
    
    public String createScritpHeatmap_CCS_1(String dir,
            String filename,
            String state) {
        
        String scriptName = dir + filename + "_heatmapScript.R";
        String file1 = "\'" + dir + filename + ".txt" + "\'";
        String write = "\'" + dir + filename + ".pdf" + "\'";
        //System.out.println(file);
        System.out.println("saving impact scores...");

        try {
            File f = new File(scriptName);
            if (!f.exists()) {
                f.createNewFile();
            }
            FileWriter fw = new FileWriter(f.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write("library(gplots);\n");
            bw.write("library(ggplot2);\n");
            bw.write("library(grid); \n");
            bw.write("library(reshape); \n");
            bw.write("library(reshape2); \n");
            bw.write("library(plyr); \n");
            bw.write("library(RColorBrewer); \n");
            bw.write("library(igraph); \n");
            bw.write("library(Vennerable); \n");
            bw.write("file1 <- read.table(" + file1 + ", sep='\\t', header = TRUE); \n");
            
            bw.write("colnames(file1) <- c('genes','state'); \n");
            bw.write("file1$group <- " + "\'" + state + "\'; \n");
            //bw.write("matrix <- rbind(file1, file2); \n");
            bw.write("matrix <- file1; \n");
            bw.write("print(matrix); \n");
            bw.write("matrix.m <- matrix; \n");
            bw.write("matrix.m$genes <- factor(matrix.m$genes, levels=matrix.m[order(matrix.m$state, decreasing=TRUE), 'genes']); \n");
            bw.write("w=8; \n");
            bw.write("h=2; \n");
            bw.write("pdf(file=" + write + ", width=w, height=h); \n");
            bw.write("figure <- ggplot(matrix.m, aes(group, genes)) + geom_tile(aes(fill = state), colour = 'black') + scale_fill_gradient2(name='IP',low='darkblue', mid='white', high='red', guide='colorbar') + theme_bw() + xlab('') + ylab('') + theme(legend.position='bottom', legend.direction='horizontal') + theme(axis.text.x = element_text(size=rel(0.75), angle=45, hjust=1), axis.text.y = element_text(size = rel(0.85), angle = 00)) + coord_flip(); \n");
            //bw.write("figure <- ggplot(matrix.m, aes(group, state)) + geom_tile(aes(fill = CCS)) + scale_fill_gradient2(low='darkblue', mid='white', high='red', guide='colorbar') + theme_bw() + xlab('') + ylab('') + theme(legend.position='bottom', legend.direction='horizontal') + theme(axis.text.x = element_text(size=rel(0.75), angle=45, hjust=1), axis.text.y = element_text(size = rel(0.85), angle = 00))");
            bw.write("print(figure); \n");
            //bw.write("ggsave(filename='/home/edroaldo/NetDecoder_Tests/seilaTESTE.pdf', plot=figure)");
            bw.write("dev.off(); \n");
            bw.write("#--------------------------------------------------------------------------\n");
            bw.close();

        } catch (IOException io) {
            System.out.println(io.getMessage());
        }
        
        return scriptName;
    }
    
    public void createScritpHeatmap_CCS(String dir,
            String filename1,
            String filename2,
            String filename,
            String state1,
            String state2) {
        
        String scriptName = dir + filename + "_heatmapScript.R";
        String file1 = "\'" + dir + filename1 + "\'";
        String file2 = "\'" + dir + filename2 + "\'";
        String write = "\'" + dir + filename + ".pdf" + "\'";
        //System.out.println(file);
        System.out.println("saving impact scores...");

        try {
            File f = new File(scriptName);
            if (!f.exists()) {
                f.createNewFile();
            }
            FileWriter fw = new FileWriter(f.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write("library(gplots);\n");
            bw.write("library(ggplot2);\n");
            bw.write("library(grid); \n");
            bw.write("library(reshape); \n");
            bw.write("library(reshape2); \n");
            bw.write("library(plyr); \n");
            bw.write("library(RColorBrewer); \n");
            bw.write("library(igraph); \n");
            bw.write("library(Vennerable); \n");
            bw.write("file1 <- read.csv(" + file1 + ", sep='\t'); \n");
            bw.write("file2 <- read.csv(" + file2 + ", sep='\t'); \n");
            bw.write("colnames(file1) <- c('expression','state','CCS'); \n");
            bw.write("colnames(file2) <- c('expression','state','CCS'); \n");
            bw.write("file1$group <- " + "\'" + state1 + "\'; \n");
            bw.write("file2$group <- " + "\'" + state2 + "\'; \n");
            bw.write("matrix <- rbind(file1, file2); \n");
            bw.write("print(matrix); \n");
            bw.write("matrix.m <- matrix; \n");
            bw.write("matrix.m$state <- factor(matrix.m$state, levels=matrix.m[order(matrix.m$CCS, decreasing=TRUE), 'state']); \n");
            bw.write("w=15; \n");
            bw.write("h=2.2; \n");
            bw.write("pdf(file=" + write + ", width=w, height=h); \n");
            bw.write("figure <- ggplot(matrix.m, aes(group, state)) + geom_tile(aes(fill = CCS), colour = 'black') + scale_fill_gradient2(name='IP',low='darkblue', mid='white', high='red', guide='colorbar') + theme_bw() + xlab('') + ylab('') + theme(legend.position='bottom', legend.direction='horizontal') + theme(axis.text.x = element_text(size=rel(0.75), angle=45, hjust=1), axis.text.y = element_text(size = rel(0.85), angle = 00)) + coord_flip(); \n");
            //bw.write("figure <- ggplot(matrix.m, aes(group, state)) + geom_tile(aes(fill = CCS)) + scale_fill_gradient2(low='darkblue', mid='white', high='red', guide='colorbar') + theme_bw() + xlab('') + ylab('') + theme(legend.position='bottom', legend.direction='horizontal') + theme(axis.text.x = element_text(size=rel(0.75), angle=45, hjust=1), axis.text.y = element_text(size = rel(0.85), angle = 00))");
            bw.write("print(figure); \n");
            //bw.write("ggsave(filename='/home/edroaldo/NetDecoder_Tests/seilaTESTE.pdf', plot=figure)");
            bw.write("dev.off(); \n");
            bw.write("#--------------------------------------------------------------------------\n");
            bw.close();

        } catch (IOException io) {
            System.out.println(io.getMessage());
        }
        
        try{
            Runtime.getRuntime().exec("R --slave --no-save CMD BATCH " +scriptName);
            
        }catch(IOException io){
            System.out.println(io.getMessage());
        }

    }
    
    public void plotBarplot(String dir, String name, String disease, 
            double corThreshold, double ratioThreshold, String filename){
        String file = "\'" + name + ".txt\'";
        String write_1 = "\'" + dir + filename  + "_keyEdges.pdf" + "\'";
        String write_2 = "\'" + dir + filename  + "_keyEdges.txt" + "\'";
        //System.out.println(file);
        System.out.println("saving key edges...");
        
        re.eval("flows <- read.table(" + file + ", sep='\t', header = TRUE)");
        re.eval("rownames(flows) <- as.vector(flows$edge)");
        re.eval("flows <- flows[,c(2, 3)]");
        re.eval("flows <- flows[which(flows$"+disease+" > " + corThreshold + "),]");
        re.eval("flows <- flows[which(flows$"+disease+" / flows$control > "+ ratioThreshold +"),]");
        re.eval("flows <- flows[order(flows$"+disease+", decreasing=TRUE),]");
        re.eval("pca <- prcomp(t(flows), center=TRUE, scale=FALSE)");
        re.eval("max <- 10");
        re.eval("loadings <- pca$rotation");
        re.eval("pc1 <- loadings[order(loadings[,1], decreasing=TRUE), 1]");
        re.eval("pc2 <- loadings[order(loadings[,2], decreasing=TRUE), 2]");
        re.eval("top.genes <- unique(c(names(pc1[1:max]), names(pc1[(length(pc1) - max):length(pc1)]), names(pc2[1:max]), names(pc2[(length(pc2) - max):length(pc2)])))");
        re.eval("flows.m <- flows[top.genes,]");
        re.eval("write.table(flows.m, file=" + write_2+ ", sep='\t', quote=FALSE)");
        re.eval("flows.m <- flows.m[order(flows.m$control, decreasing=TRUE),]");
        //re.eval("flows.m <- flows.m[order(flows.m$"+disease+", decreasing=TRUE),]");
        re.eval("flows.m$edges = rownames(flows.m)");
        re.eval("flows.m <- melt(flows.m, id.var='edges')");
        re.eval("flows.m$edges <- factor(flows.m$edges, levels=flows.m$edges)");
        re.eval("colls <- c('#BDC9E1', '#67A9CF', '#1C9099', '#016C59', '#0023a0', '#f9a635', 'red', 'black')");
        re.eval("pdf(file=" + write_1 + ", width=5, height=3);");
        //re.eval("pdf(file=" + write_1 + ", width=2.5, height=5);");
        re.eval("figure <- ggplot(data=flows.m, aes(x=edges, y=value, group=variable, fill=variable))+ geom_bar(stat=\"identity\", width=.7, colour='black') + theme_bw() + guides(fill=guide_legend(ncol=2)) + theme(legend.position=\"top\",legend.key.size = unit(0.3, \"cm\"), legend.text=element_text(size=7)) + xlab(\"\") + ylab(\"Edge Flow\") + theme(axis.text.x = element_text(size=6, angle=45, hjust=1), axis.title.y = element_text(size = rel(1), angle = 90), axis.text.y = element_text(size=6, angle=00, hjust=1)) + scale_fill_manual('', values=c(colls[3], colls[6]))");
        //re.eval("figure <- ggplot(data=flows.m, aes(x=edges, y=value, group=variable, fill=variable))+ geom_bar(stat=\"identity\", width=.7, colour='black') + theme_bw() + guides(fill=guide_legend(ncol=2)) + theme(legend.position=\"top\",legend.key.size = unit(0.3, \"cm\"), legend.text=element_text(size=7)) + xlab(\"\") + ylab(\"Edge Flow\") + theme(axis.text.x = element_text(size=6, angle=00, hjust=1), axis.title.y = element_text(size = rel(1), angle = 00), axis.text.y = element_text(size=6, angle=00, hjust=1)) + scale_fill_manual('', values=c(colls[3], colls[6])) + coord_flip()");
        re.eval("print(figure)");
        re.eval("dev.off();");
    }
    
    public void plotHeatmap_CCS(String dir,
            String filename1,
            String filename2,
            String filename,
            String state1,
            String state2){
        String file1 = "\'" + dir + filename1  + "\'";
        String file2 = "\'" + dir + filename2  + "\'";
        String write = "\'" + dir + filename + ".pdf" + "\'";
        //System.out.println(file);
        System.out.println("saving impact scores...");
                
        //System.out.println("matrix <- read.csv(" + file1 + ", sep='\t')");
        re.eval("file1 <- read.csv(" + file1 + ", sep='\t')");
        re.eval("file2 <- read.csv(" + file2 + ", sep='\t')");
        re.eval("colnames(file1) <- c('expression','state','CCS')");
        re.eval("colnames(file2) <- c('expression','state','CCS')");
        re.eval("file1$group <- " + "\'" + state1 + "\'");
        re.eval("file2$group <- " + "\'" + state2 + "\'");
        re.eval("matrix <- rbind(file1, file2)");
        re.eval("print(matrix)");
        re.eval("matrix.m <- matrix");
        re.eval("matrix.m$state <- factor(matrix.m$state, levels=matrix.m[order(matrix.m$CCS, decreasing=TRUE), 'state'])");
        re.eval("w=15");
        re.eval("h=2.2");
        re.eval("pdf(file=" + write + ", width=w, height=h);");
        re.eval("figure <- ggplot(matrix.m, aes(group, state)) + geom_tile(aes(fill = CCS), colour = 'black') + scale_fill_gradient2(name='IP',low='darkblue', mid='white', high='red', guide='colorbar') + theme_bw() + xlab('') + ylab('') + theme(legend.position='bottom', legend.direction='horizontal') + theme(axis.text.x = element_text(size=rel(0.75), angle=45, hjust=1), axis.text.y = element_text(size = rel(0.85), angle = 00)) + coord_flip()");
        //re.eval("figure <- ggplot(matrix.m, aes(group, state)) + geom_tile(aes(fill = CCS)) + scale_fill_gradient2(low='darkblue', mid='white', high='red', guide='colorbar') + theme_bw() + xlab('') + ylab('') + theme(legend.position='bottom', legend.direction='horizontal') + theme(axis.text.x = element_text(size=rel(0.75), angle=45, hjust=1), axis.text.y = element_text(size = rel(0.85), angle = 00))");
        re.eval("print(figure)");
        //re.eval("ggsave(filename='/home/edroaldo/NetDecoder_Tests/seilaTESTE.pdf', plot=figure)");
        re.eval("dev.off()");
        //re.startMainLoop();
    }
    
    public void plotHeatmap_CCS_2(String dir,
            String filename1,
            String filename2,
            String filename3,
            String filename,
            String state1,
            String state2,
            String state3){
        String file1 = "\'" + dir + filename1  + "\'";
        String file2 = "\'" + dir + filename2  + "\'";
        String file3 = "\'" + dir + filename3  + "\'";
        String write = "\'" + dir + filename + ".pdf" + "\'";
        //System.out.println(file);
        System.out.println("saving impact scores...");
        
        //System.out.println("matrix <- read.csv(" + file1 + ", sep='\t')");
        re.eval("file1 <- read.csv(" + file1 + ", sep='\t')");
        re.eval("file2 <- read.csv(" + file2 + ", sep='\t')");
        re.eval("file3 <- read.csv(" + file3 + ", sep='\t')");
        re.eval("colnames(file1) <- c('expression','state','CCS')");
        re.eval("colnames(file2) <- c('expression','state','CCS')");
        re.eval("colnames(file3) <- c('expression','state','CCS')");
        re.eval("file1$group <- " + "\'" + state1 + "\'");
        re.eval("file2$group <- " + "\'" + state2 + "\'");
        re.eval("file3$group <- " + "\'" + state3 + "\'");
        re.eval("matrix <- rbind(file1, file2, file3)");
        re.eval("print(matrix)");
        re.eval("matrix.m <- matrix");
        re.eval("matrix.m$state <- factor(matrix.m$state, levels=matrix.m[order(matrix.m$CCS, decreasing=TRUE), 'state'])");
        re.eval("w=15");
        re.eval("h=2.5");
        re.eval("pdf(file=" + write + ", width=w, height=h);");
        re.eval("figure <- ggplot(matrix.m, aes(group, state)) + geom_tile(aes(fill = CCS), colour = 'black') + scale_fill_gradient2(name='IP', low='darkblue', mid='white', high='red', guide='colorbar') + theme_bw() + xlab('') + ylab('') + theme(legend.position='bottom', legend.direction='horizontal') + theme(axis.text.x = element_text(size=rel(0.75), angle=45, hjust=1), axis.text.y = element_text(size = rel(0.85), angle = 00)) + coord_flip()");
        //re.eval("figure <- ggplot(matrix.m, aes(group, state)) + geom_tile(aes(fill = CCS)) + scale_fill_gradient2(low='darkblue', mid='white', high='red', guide='colorbar') + theme_bw() + xlab('') + ylab('') + theme(legend.position='bottom', legend.direction='horizontal') + theme(axis.text.x = element_text(size=rel(0.75), angle=45, hjust=1), axis.text.y = element_text(size = rel(0.85), angle = 00))");
        re.eval("print(figure)");
        //re.eval("ggsave(filename='/home/edroaldo/NetDecoder_Tests/seilaTESTE.pdf', plot=figure)");
        re.eval("dev.off()");
        //re.startMainLoop();
    }
    
    public void plotAdjMatrix(String dir, String filename, String gene){
        String file = "\'" + dir + filename + ".txt" + "\'";
        String write = "\'" + dir + filename + ".pdf" + "\'";
        System.out.println("saving adjacency matrices...");
                
        re.eval("matrix <- read.csv(" + file + ", sep='\t')");
        re.eval("rownames(matrix) <- matrix$gene");
        re.eval("matrix <- matrix[, 2:dim(matrix)[2]]");
        re.eval("matrix[row(matrix) > col(matrix)] <- 0.5");
        re.eval("diag(matrix) <- 0.5");
        //re.eval("matrix[lower.tri(matrix)]<- NA");
        re.eval("matrix <- data.frame(gene=rownames(matrix), matrix)");
        re.eval("matrix.m <- melt(matrix, id.var='gene')");
        re.eval("matrix.m <- na.omit(matrix.m)");
        re.eval("matrix.m$gene <- factor(matrix.m$gene, levels=matrix.m$gene)");
        re.eval("matrix.m$variable <- factor(matrix.m$variable, levels=rev(levels(matrix.m$gene)))");
        re.eval("matrix.m <- ddply(matrix.m, .(variable), transform, rescale = value)");
        re.eval("w=dim(matrix)[1] / 2.5");
        re.eval("h=dim(matrix)[2] / 3");
        re.eval("pdf(file=" + write + ", width=w, height=h);");
        re.eval("figure <- ggplot(matrix.m, aes(gene, variable)) + geom_tile(aes(fill = value), colour = 'black') + scale_fill_gradient(low = 'white', high = 'red') + theme_bw() + xlab('') + ylab('') + theme(panel.grid=element_blank(), panel.border=element_blank(), legend.position='none', axis.text.x = element_text(size=rel(1), angle=45, hjust=1),axis.title.y = element_text(size = rel(0.3), angle = 90)) + ggtitle("+"\'"+gene+"\'"+")");
        re.eval("print(figure)");
        //re.eval("ggsave(filename='/home/edroaldo/NetDecoder_Tests/seilaTESTE.pdf', plot=figure)");
        re.eval("dev.off()");
        //re.startMainLoop();
    }
    
    public void plotJaccardMatrix(String filename){
        String file = "\'" + filename + ".txt" + "\'";
        String write = "\'" + filename + ".pdf" + "\'";
        
        System.out.println("saving Jaccard index heatmap...");
        re.eval("sim <- read.csv(" + file + ", sep='\t')");
        re.eval("names <- as.vector(sim$X);");
        re.eval("sim <- sim[,2:dim(sim)[2]]");
        re.eval("rownames(sim) <- names;");
        re.eval("hmcols<- colorRampPalette(c(\"black\", \"yellow\"))");
        re.eval("pdf(file=" + write + ", width=5, height=5);");
        re.eval("heatmap.2(as.matrix(t(sim)), col=hmcols,trace=\"none\", density.info=\"none\", scale=\"none\",margin=c(15,15), key=TRUE, Rowv=T, Colv=T,srtCol=60, dendrogram=\"none\", cexCol=0.55, cexRow=0.55, symm=T,  sepcolor='black')");
        re.eval("dev.off()");
    }
    
    public static void addLibraryPath(String path) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        String oldPath = System.getProperty("java.library.path");
        if (oldPath.length() > 0) {
            path = path + ":" + oldPath;
        }
        System.setProperty("java.library.path", path);
        //set sys_paths to null
        final Field sysPathsField = ClassLoader.class.getDeclaredField("sys_paths");
        sysPathsField.setAccessible(true);
        sysPathsField.set(null, null);
    }
}
