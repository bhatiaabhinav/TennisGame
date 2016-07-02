import lombok.NonNull;
import org.deeplearning4j.nn.api.Model;
import org.deeplearning4j.nn.api.Updater;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * Utility class suited to save/restore neural net models
 *
 * @author raver119@gmail.com
 */
public class ModelSerializer {

    public static void writeModel(@NonNull Model model, @NonNull File file, boolean saveUpdater) throws IOException {
        try(BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(file))){
            writeModel(model, stream, saveUpdater);
        }
    }

    public static void writeModel(@NonNull Model model, @NonNull String path, boolean saveUpdater) throws IOException {
        try(BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(path))){
            writeModel(model, stream, saveUpdater);
        }
    }

    public static void writeModel(@NonNull Model model, @NonNull OutputStream stream, boolean saveUpdater) throws IOException {
        ZipOutputStream zipfile = new ZipOutputStream(stream);

        // save json first
        String json = "";
        json = ((MultiLayerNetwork) model).getLayerWiseConfigurations().toJson();

        ZipEntry config = new ZipEntry("configuration.json");
        zipfile.putNextEntry(config);

        writeEntry(new ByteArrayInputStream(json.getBytes()), zipfile);

        ZipEntry coefficients = new ZipEntry("coefficients.bin");
        zipfile.putNextEntry(coefficients);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        Nd4j.write(model.params(), dos);
        dos.flush();
        dos.close();

        InputStream inputStream = new ByteArrayInputStream(bos.toByteArray());
        writeEntry(inputStream, zipfile);

        if (saveUpdater) {
            ZipEntry updater = new ZipEntry("updater.bin");
            zipfile.putNextEntry(updater);


            bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(((MultiLayerNetwork) model).getUpdater());
            oos.flush();
            oos.close();

            inputStream = new ByteArrayInputStream(bos.toByteArray());
            writeEntry(inputStream, zipfile);
        }

        zipfile.flush();
        zipfile.close();
    }


    private static void writeEntry(InputStream inputStream, ZipOutputStream zipStream) throws IOException {
        byte[] bytes = new byte[1024];
        int bytesRead;
        while ((bytesRead = inputStream.read(bytes)) != -1) {
            zipStream.write(bytes, 0, bytesRead);
        }
    }

    public static MultiLayerNetwork restoreMultiLayerNetwork(@NonNull File file) throws IOException {
        ZipFile zipFile = new ZipFile(file);

        boolean gotConfig = false;
        boolean gotCoefficients = false;
        boolean gotUpdater = false;

        String json = "";
        INDArray params = null;
        Updater updater = null;


        ZipEntry config = zipFile.getEntry("configuration.json");
        if (config != null) {
        //restoring configuration

            InputStream stream = zipFile.getInputStream(config);
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            String line = "";
            StringBuilder js = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                js.append(line).append("\n");
            }
            json = js.toString();

            reader.close();
            stream.close();
            gotConfig = true;
        }


        ZipEntry coefficients = zipFile.getEntry("coefficients.bin");
        if (coefficients != null) {
            InputStream stream = zipFile.getInputStream(coefficients);
            DataInputStream dis = new DataInputStream(stream);
            params = Nd4j.read(dis);

            dis.close();
            gotCoefficients = true;
         }


        ZipEntry updaters = zipFile.getEntry("updater.bin");
        if (updaters != null) {
            InputStream stream = zipFile.getInputStream(updaters);
            ObjectInputStream ois = new ObjectInputStream(stream);

            try {
                updater = (Updater) ois.readObject();
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }

            gotUpdater = true;
        }


        zipFile.close();

        if (gotConfig && gotCoefficients) {
            MultiLayerConfiguration confFromJson = MultiLayerConfiguration.fromJson(json);
            MultiLayerNetwork network = new MultiLayerNetwork(confFromJson);
            network.init();
            network.setParameters(params);


            if (gotUpdater && updater != null) {
                network.setUpdater(updater);
            }
            return network;
        } else throw new IllegalStateException("Model wasnt found within file: gotConfig: ["+ gotConfig+"], gotCoefficients: ["+ gotCoefficients+"], gotUpdater: ["+gotUpdater+"]");
    }

    public static MultiLayerNetwork restoreMultiLayerNetwork(@NonNull String path) throws IOException {
        return restoreMultiLayerNetwork(new File(path));
    }
    
}