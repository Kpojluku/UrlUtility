import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class URLDownloader {

    // Требование 1
    public String getFileName(String name) {
        String FileName;
        String url = name;

        int i = url.indexOf("://");
        url = url.substring(i + 3);

        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }

        if (url.contains("/")) {
            if (url.contains("?")) {
                while (url.contains("/")) {
                    url = url.substring(url.indexOf("/") + 1);
                }
                FileName = url.substring(0, url.indexOf("?"));
            } else {
                FileName = url.substring(url.lastIndexOf("/") + 1);
            }
        } else {
            FileName = "index.html";
        }
        return FileName;
    }

    // Требование 2
    public String saveSite(String directory, String domainName) {
        Path file;
        try {
            file = Paths.get(directory);
        } catch (InvalidPathException e) {
            System.err.println(e.getMessage());
            System.out.println("Проверьте корректность введеного пути к файлу");
            return null;
        }
        if (Files.exists(file)) {
            /*
            Если директория заканчивается на папку, то FileName будет равен последней части directory
            Если директория заканчивается на файл, запрос к пользователю, подтвердить замену файла или задать другой
            FileName
             */
            if (Files.isDirectory(file)) {
                directory += "\\" + getFileName(domainName);
            } else {
                directory = askUser(directory);
            }
        }
        return saveSiteDefault(directory, domainName);
    }

    public String saveSiteDefault(String FileName, String domainName) {
        try {
            URL url = new URL(domainName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), getCharset(url)));
            FileWriter fileWriter = new FileWriter(FileName);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

            String line;
            while ((line = reader.readLine()) != null) {
                bufferedWriter.write(line);
            }

            bufferedWriter.close();
            reader.close();

        } catch (MalformedURLException | FileNotFoundException e) {
            System.err.println(e.getMessage());
            System.out.println("Проверьте корректность введеного URL");
        } catch (IOException ignored) {
        }
        return FileName;
    }

    // Требование 5
    private String getCharset(URL url) throws IOException {
        URLConnection con = url.openConnection();
        String contentType = con.getContentType();
        String charset;
        if (contentType.contains("=")) {
            charset = contentType.substring(contentType.lastIndexOf("=") + 1);
        } else {
            charset = "UTF-8";
        }
        return charset;
    }

    public String readFile(String FileName) {
        StringBuilder sb = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new FileReader(FileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    // Требование 4
    public String htmlParse(String htmlFile, String FileName) {
        htmlFile = doImg(htmlFile, FileName);
        htmlFile = doLink(htmlFile, FileName);
        return htmlFile;
    }

    private String doLink(String htmlFile, String FileName) {
        Pattern p = Pattern.compile("<link[^>]*?href=[\"']?([^\"'\\s>]+)[\"']?");
        Matcher m = p.matcher(htmlFile);
        int count = 0;
        String filesDir;
        String hrefStr;
        String path;

        if (m.find()) {
            filesDir = FileName + "_files";
            if (!Files.exists(Paths.get(filesDir))) {
                new File(filesDir).mkdir();
            }
            do {
                hrefStr = m.group(1);
                if (hrefStr.startsWith("//")) {
                    hrefStr = "https:" + hrefStr;
                }

                try {
                    URL url = new URL(hrefStr);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8));
                    BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(filesDir + "\\" + count + "l"));

                    String line;
                    while ((line = reader.readLine()) != null) {
                        bufferedWriter.write(line);
                    }

                    bufferedWriter.close();
                    reader.close();
                } catch (Exception ignored) {
                }

                path = filesDir.substring(filesDir.lastIndexOf("\\") + 1) + "\\" + count + "l";
                htmlFile = htmlFile.replace(hrefStr, path);

                count++;
            } while (m.find());
        }
        return htmlFile;
    }

    private String doImg(String htmlFile, String FileName) {
        Pattern p = Pattern.compile("<img[^>]*?src=[\"']?([^\\\\\"'\\s>]+)[\"']?");
        Matcher m = p.matcher(htmlFile);

        if (m.find()) {
            String filesDir = FileName + "_files";
            if (!Files.exists(Paths.get(filesDir))) {
                new File(filesDir).mkdir();
            }
            int count = 0;
            String imgFormat;
            String urlStr;
            File file;
            do {
                urlStr = m.group(1);
                try {
                    URL url = new URL(urlStr);
                    BufferedImage img = ImageIO.read(url);
                    imgFormat = getImgFormat(urlStr);
                    file = new File(filesDir + "\\" + count + "." + imgFormat);
                    ImageIO.write(img, imgFormat, file);
                    String parent = file.getParent();
                    String path = parent.substring(parent.lastIndexOf("\\") + 1) + "\\" + file.getName();
                    htmlFile = htmlFile.replace(urlStr, path);

                } catch (Exception ignored) {
                }
                count++;
            } while (m.find());
        }
        return htmlFile;
    }

    private String getImgFormat(String url) {
        Pattern p = Pattern.compile("\\S+[.]([A-Za-z]+)");
        Matcher m = p.matcher(url);
        if (m.find()) {
            return m.group(1);
        }
        return null;
    }

    public void saveNewHtmlFile(String htmlFile, String dir) {

        try (FileWriter fileWriter = new FileWriter(dir);
             BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)) {

            bufferedWriter.write(htmlFile);

        } catch (IOException ignored) {

        }
    }

    public boolean isHtml(String siteAddress) {
        boolean check = false;
        try {
            URL url = new URL(siteAddress);
            URLConnection con = url.openConnection();
            String contentType = con.getContentType();
            check = contentType.contains("html");
        } catch (IOException ignored) {
        }
        return check;
        //   return file.startsWith("<!DOCTYPE html") || file.startsWith("<!doctype html");
    }

    // Требование 2.2
    private String askUser(String directory) {
        String FileName;
        boolean check1 = true;
        boolean check2 = true;
        try
                (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            System.out.println("Указанный вами путь к файлу уже существует");
            System.out.println("Вы хотите выполнить операцию с заменой файла? Да/Нет:");
            // Пока пользователь не даст корректный ответ
            while (check1) {
                String answer = reader.readLine();
                if (!(answer.equalsIgnoreCase("Нет") || answer.equalsIgnoreCase("Да"))) {
                    System.out.println("Введите \"Да\" или \"Нет\"");
                    continue;
                }
                if (answer.equalsIgnoreCase("Нет")) {
                    System.out.println("Задайте другое имя файла (не путь к файлу):");
                    directory = directory.substring(0, directory.lastIndexOf("\\") + 1);
                    while (check2) {
                        FileName = reader.readLine();
                        // здесь проверки на корректность FileName и на существование файла с таким же именем
                        // Если проверки прошли, то выход из циклов
                        if (isFileNameCorrect(FileName)) {
                            if (!Files.exists(Paths.get(directory + FileName))) {
                                directory += FileName;
                                check1 = false;
                                check2 = false;
                            } else {
                                System.out.println("Такое имя файла в заданной папке уже есть");
                            }
                        } else {
                            System.out.println("Имя файла не должно содержать следующих знаков: \\/:*\"?<>|");
                        }
                    }
                } else {
                    check1 = false;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return directory;
    }

    private boolean isFileNameCorrect(String FileName) {
        Pattern pattern = Pattern.compile("(.+)?[><|?*\\\\:\"/](.+)?");
        Matcher matcher = pattern.matcher(FileName);
        return !matcher.find();
    }

    // Требование 3
    public void openFile(String FileName) {
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop desktop = Desktop.getDesktop();
                File myFile = new File(FileName);
                desktop.open(myFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
