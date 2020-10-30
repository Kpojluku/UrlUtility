public class Main {
    public static void main(String[] args) throws Exception {

        if (args.length > 0) {
            String domainName = args[0];

            URLDownloader urlDownloader = new URLDownloader();

            String FileName = urlDownloader.getFileName(domainName);
            String fileDir;
            String htmlFile;
            if (args.length > 1) {
                fileDir = urlDownloader.saveSite(args[1], domainName);
            } else {
                fileDir = urlDownloader.saveSiteDefault(FileName, domainName);
            }
            if (fileDir != null) {
                htmlFile = urlDownloader.readFile(fileDir);

                if (urlDownloader.isHtml(htmlFile)) {
                    String newHtmlFile = urlDownloader.htmlParse(htmlFile, fileDir);
                    urlDownloader.saveNewHtmlFile(newHtmlFile, fileDir);
                }
                if (args.length > 2 && args[2].equalsIgnoreCase("open")) {
                    urlDownloader.openFile(fileDir);
                }
            }

        } else {
            System.out.println("Первым аргументом командной строки должен быть задан url сайта");
            throw new Exception("Неверные входные параметры");
        }
    }
}


