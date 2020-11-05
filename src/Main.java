public class Main {
    public static void main(String[] args) throws Exception {

        if (args.length > 0) {
            String url = args[0];

            URLDownloader urlDownloader = new URLDownloader();

            String FileName = urlDownloader.getFileName(url);
            String fileDir;
            String htmlFile;
            if (args.length > 1) {
                fileDir = urlDownloader.saveSite(args[1], url);
            } else {
                fileDir = urlDownloader.saveSiteDefault(FileName, url);
            }
            if (fileDir != null) {
                htmlFile = urlDownloader.readFile(fileDir);

                if (urlDownloader.isHtml(url)) {
                    String newHtmlFile = urlDownloader.htmlParse(htmlFile, fileDir);
                    urlDownloader.saveNewHtmlFile(newHtmlFile, fileDir);
                }
                if (args.length > 2 && args[2].equalsIgnoreCase("open")) {
                    urlDownloader.openFile(fileDir);
                }
            }
        } else {
            System.out.println("Первым аргументом командной строки должен быть задан url сайта");
            throw new IllegalArgumentException("Неверные входные параметры");
        }
    }
}


