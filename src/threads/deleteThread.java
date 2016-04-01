package threads;

import communication.Server;
import console.MessageCenter;
import message.Message;
import protocols.FileDeletion;

import java.io.File;

/**
 * Created by Rui on 01/04/2016.
 */
public class deleteThread implements Runnable {

    private String fileName;

    public deleteThread(File file){
        this.fileName = file.getName();
    }

    @Override
    public void run(){
        if (fileExists(fileName)){
            deleteFile(fileName);
            MessageCenter.output("File deleted: " + fileName);
        }

        if (1 == 1){ // Aqui deve verificar se existe backup do ficheiro (ver se função existe)
            MessageCenter.output(fileName + ": starting chunks deletion");
            String fileID = ""; // Usar função que dá o string ID

            //Send delete message
            Message m = new Message("DELETE", "1.0", Server.getInstance().getId(), fileID, "1"); //
            FileDeletion FD = new FileDeletion(m);
            FD.send();

            //Remover da base de dados de chunks. ?????????????????????????????????????????????????????
            //chunks.remove(fileName);

        }
        else {
            MessageCenter.output(fileName + ": No chunks backed up");
        }


    }

    public static boolean fileExists(String name) {
        File file = new File(name);

        return file.exists() && file.isFile();
    }

    public static final void deleteFile(String fileName) {
        File file = new File(fileName); // ADICIONAR PATH ???????????????????????????????????????????????????????????

        file.delete();
    }


}
