// import com.sun.org.apache.xpath.internal.operations.String;

import java.io.*;
import java.util.*;
import javax.print.DocFlavor.STRING;

// 站点，保存站点名称和ID
class Station {
    String stationName;
    Set<Integer> setStationId;

    Station() {
        stationName = "";
        setStationId = new TreeSet<>();
    }
}


// 路径，距离和经过的中转站列表
class Path {
    int nFDistance;
    Station stationLastStationInPath;

    Path() {
        nFDistance = 0;
        stationLastStationInPath = new Station();
    }
}

// 地图，负责站点解析和路径计算
class Map {
    private Vector<String> listSubwayInfo = new Vector<>();
    private int nMaxDistance = 999999; // 访问不到的设置为无穷大
    private static HashMap<String, Station> mapNametoStation = new HashMap<>();
    private static HashMap<Integer, Station> mapStationIdtoStation = new HashMap<>();
    private static HashMap<Integer, String> stationInfo = new HashMap<>();
    private static HashMap<String,String> finalMap = new HashMap<>();
    private static HashMap<String, Integer> mapTransferStationNametoDistance = new HashMap<>();
    private static HashMap<Integer, Integer> circleInfo = new HashMap<>();
    private static HashMap<String, Integer> NtostationInfo = new HashMap<>();
    // ------------------------------------------------------------------------------------------------
    // 加载地铁线路数据
    public void loadLineFile(String strSubwayFileName) {
        File fSubway = new File(strSubwayFileName);
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(fSubway));
            String tempString = null;

            while ((tempString = reader.readLine()) != null) {
                if (tempString.startsWith("\uFEFF")) {
                    tempString = tempString.substring(1, tempString.length());
                }
                listSubwayInfo.addElement(tempString);
            }
            System.out.println("成功加载地铁线路文件！\n");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        parseSubwayStationsData();
    }

  
    // ----------------------------------------------
    // 地铁数据处理
    void parseSubwayStationsData() {
        for (String strSubwayLine: listSubwayInfo) {
            parseSubwayLineData(strSubwayLine);
        }
    }

    void parseSubwayLineData(String strSubwayLine) {

        String[] arrLineAndStations = strSubwayLine.split("：");     // 划分地铁线路和该线路所有站点
        if (arrLineAndStations.length != 3) {
            System.out.println("地铁数据错误" + strSubwayLine);
            return;
        }
        
        int nLine = getLineNumber(arrLineAndStations[0]);
        stationInfo.put(nLine,arrLineAndStations[1]);
        NtostationInfo.put(arrLineAndStations[1],nLine);
        if (nLine == -1) {
            System.out.println("地铁线路号数据错误" + strSubwayLine);
        }
        String[] arrStrStationNames = arrLineAndStations[2].split("，");
//        System.out.println(arrStrStationNames[0]+"   "+arrStrStationNames[arrStrStationNames.length-1]);;
        if(arrStrStationNames[0].equals(arrStrStationNames[arrStrStationNames.length-1]))
        {
        	
        	for (int i=0; i < arrStrStationNames.length-1; i++) {
            	
                String strStationName = arrStrStationNames[i];
                int nStationId = -(nLine*1000 + i+1);
                circleInfo.put(nLine,arrStrStationNames.length-1 );
                Station station = new Station();
                station.stationName = strStationName;
                station.setStationId.add(nStationId);
                mapStationIdtoStation.put(nStationId, station);
                if (!mapNametoStation.containsKey(strStationName)) {
                    mapNametoStation.put(strStationName, station);
                } else {
                    // 如果站点名字存在，证明是中转站
                    Station stationExistedTransferStation = mapNametoStation.get(strStationName);
                    stationExistedTransferStation.setStationId.add(nStationId);
                    mapTransferStationNametoDistance.put(stationExistedTransferStation.stationName, nMaxDistance);
                }
            }
        }
        for (int i=0; i < arrStrStationNames.length; i++) {
            String strStationName = arrStrStationNames[i];
            int nStationId = nLine*1000 + i+1;
            Station station = new Station();
            station.stationName = strStationName;
            station.setStationId.add(nStationId);
            mapStationIdtoStation.put(nStationId, station);
            if (!mapNametoStation.containsKey(strStationName)) {
                mapNametoStation.put(strStationName, station);
            } else {
                // 如果站点名字存在，证明是中转站
                Station stationExistedTransferStation = mapNametoStation.get(strStationName);
                stationExistedTransferStation.setStationId.add(nStationId);
                mapTransferStationNametoDistance.put(stationExistedTransferStation.stationName, nMaxDistance);
            }
        }
    }

    // -----------------------------------------------------------------------------------
    // 最优路径规划
    Path Dijkstra(String strStartStationName, String strEndStationName) {
        // todo: 进行一些合法性检查
        Station stationStart = mapNametoStation.get(strStartStationName);
        Station stationEnd = mapNametoStation.get(strEndStationName);
        if(stationStart==null || stationEnd==null)
        {
        	System.out.println("起始站或终点输入不正确，请检查输入数据！");
        	return null;
        }
        mapTransferStationNametoDistance.put(strEndStationName, nMaxDistance);
        mapTransferStationNametoDistance.put(strStartStationName, nMaxDistance);
        Path pathStart = new Path();
        pathStart.nFDistance = 0;
        pathStart.stationLastStationInPath = stationStart;
        Path Dijkstra = new Path();
        Dijkstra.nFDistance = nMaxDistance;

        Stack<Path> stackAllPaths = new Stack<>();
        stackAllPaths.push(pathStart);

        Set<String> TStationNameSet = new TreeSet<>();
        for(String strname: mapTransferStationNametoDistance.keySet()) {
        	  TStationNameSet.add(strname);   	      
        	       }
        for(String strname: mapTransferStationNametoDistance.keySet()) {
        	  finalMap.put(strname,"null");
        	       }
        while (!stackAllPaths.empty()) {
            Path pathCurrent = stackAllPaths.pop();
            if (pathCurrent.nFDistance > Dijkstra.nFDistance) {
                continue;
            }
            int nBDistance = getStationsDistance(pathCurrent.stationLastStationInPath, stationEnd);
            if (nBDistance == 0) {      // 到达终止节点
                if (pathCurrent.nFDistance < Dijkstra.nFDistance) {
                    Dijkstra = pathCurrent;
                }
                continue;
            }
            int minDistance = 1000000;
            String nextStation = null;    
            TStationNameSet.remove(pathCurrent.stationLastStationInPath.stationName);
            for (String strTStationName: mapTransferStationNametoDistance.keySet()) {
                Station stationTransfer = mapNametoStation.get(strTStationName);
                int nDistanceDelta = getStationsDistance(pathCurrent.stationLastStationInPath, stationTransfer);
                int nTStationDistance = pathCurrent.nFDistance + nDistanceDelta;
                if (nTStationDistance >= mapTransferStationNametoDistance.get(strTStationName)) {
                    continue;
                }
                finalMap.put(strTStationName,pathCurrent.stationLastStationInPath.stationName);
                mapTransferStationNametoDistance.put(strTStationName, nTStationDistance);      
            }
            for(String strTStationName: mapTransferStationNametoDistance.keySet()) {
            	int Distance = mapTransferStationNametoDistance.get(strTStationName);
            	if(Distance<minDistance&& TStationNameSet.contains(strTStationName)) {
            	minDistance = Distance;
            	nextStation = strTStationName;          	
            	}
            }
            Station stationTransfer = mapNametoStation.get(nextStation);
            Path pathNew = new Path();
            pathNew.nFDistance = minDistance;
            pathNew.stationLastStationInPath = stationTransfer;
            stackAllPaths.push(pathNew);
        }
        System.out.println(finalMap);
        return Dijkstra;
    }

    // -----------------------------------------------------------------------------------
    // 打印一个路径
    void printPath(String start,String end, String strOutFileName) {
    	String strFormatedPath = formatPath(start,end);

        toFile(strFormatedPath, strOutFileName);
    }

    String formatPath(String start,String end) {
        StringBuffer strRst = new StringBuffer();

        int nCurrentLine = -1;

        System.out.print(finalMap);
        while(end != finalMap.get(end)) {
            Station stationStart = mapNametoStation.get(end);
            Station stationEnd = mapNametoStation.get(finalMap.get(end)); 
            end = finalMap.get(end);           
            int nLineNum = Math.abs(getLineNumber(stationStart, stationEnd));           
            if (nLineNum != nCurrentLine) {
                nCurrentLine = nLineNum;
                strRst.append(String.format("地铁：%s\r\n", stationInfo.get(nLineNum)));
            }
            for (String strStationName: stationsInLine(stationStart, stationEnd)) {
                strRst.append(String.format("%s\r\n", strStationName));
            }
        }
        return strRst.toString();
    }
    Vector<String> stationsInLine(Station stationStart, Station stationEnd) {
        Vector<String> listStations = new Vector<String>();
        int nLineNumber = getLineNumber(stationStart, stationEnd);

        int nStartId = 0;
        int nEndId = 0;
        for (int nId: stationStart.setStationId) {
            if (Math.abs(nId-(nLineNumber*1000))<1000) {
                nStartId = nId;
            }
        }
        for (int nId: stationEnd.setStationId) {
            if (Math.abs(nId-(nLineNumber*1000))<1000) {
                nEndId = nId;
            }
        }
        if (nStartId == nEndId) {
            return listStations;
        }
        int nStep = 1;
        if (nEndId < nStartId) {
            nStep = -1;
        }
        int nIndexId = nStartId + nStep;
        while (nIndexId != nEndId) {
            String strSName = mapStationIdtoStation.get(nIndexId).stationName;
            listStations.addElement(strSName);
            nIndexId += nStep;
        }
        String strName = mapStationIdtoStation.get(nEndId).stationName;
        listStations.addElement(strName);
        return listStations;
    }

    // ------------------------------------------------------------------------------------
    // 获取特定地铁线路数据
//    void printLineInfo(int nLineNum, String strOutFile) {
    void printLineInfo(String LineName, String strOutFile) {
        StringBuffer strRst = new StringBuffer();
//        strRst.append(String.format("%s\r\n", stationInfo.get(nLineNum)));
        strRst.append(String.format("地铁：%s\r\n", LineName));
        if(NtostationInfo.get(LineName)==null)
        {
        	System.out.println("不存在该条地铁线路，请检查输入！\n");
        	return;
        }
        int nLineNum = NtostationInfo.get(LineName);
        for (int i = 1; i < 90; i++) {
            int nStationId = nLineNum * 1000 + i;

            if (mapStationIdtoStation.containsKey(nStationId)) {
                strRst.append(mapStationIdtoStation.get(nStationId).stationName + "\r\n");
            } else {
                break;
            }
        }
        toFile(strRst.toString(), strOutFile);
    }


    // ------------------------------------------------------------------------------------
    // 工具函数
    int getLineNumber(int nStationId) {
        return nStationId / 1000;
    }

    int getLineNumber(String strLine) {
    	return Integer.parseInt(strLine);
    }


    int getStationsDistance(Station S1, Station S2) {
        int nMinDistance = nMaxDistance;
        Set<Integer> S1Ids = S1.setStationId;
        Set<Integer> S2Ids = S2.setStationId;
        for (int id1: S1Ids) {
            for (int id2: S2Ids) {
                int nDistance = Math.abs(id1-id2);
                if(nDistance<1000 && id1<0 &&id2<0)
                {
                	int lineNum = getLineNumber(-id1);
                	int lineLength = circleInfo.get(lineNum);
                	nDistance = Math.min(Math.abs(id1-id2), lineLength-Math.abs(id1-id2));
                }
                if (nDistance < nMinDistance) {
                    nMinDistance = nDistance;
                }
            }
        }

        return nMinDistance;
    }

    int getLineNumber(Station S1, Station S2) {
        for (int nS1Id: S1.setStationId) {
            int nS1LineNum = getLineNumber(nS1Id);

            for (int nS2Id: S2.setStationId) {
                int nS2LineNumber = getLineNumber(nS2Id);

                if (nS1LineNum == nS2LineNumber) {
                    return nS1LineNum;
                }
            }
        }
        return -1;
    }

    void toFile(String strContent, String strOutFile) {
        try {
            File file = new File(strOutFile);
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter fileWriter = new FileWriter(file.getName(), false);

            fileWriter.write(strContent.toString());
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    };

}


public class subway {
    public static void main(String[] args) {
        // -------------------------------------
        // 解析参数
        String strSubwayFileName = null;
        int nLineNum = -1;
        String LineName =null;
        String strOutFileName = null;
        String strStartStationName = null;
        String strEndStationName = null;

        for (int n=0; n<args.length; n++) {
            String strArg = args[n];

            if (strArg.equals("-map")) {
                n += 1;
                if (n < args.length) {
                    strSubwayFileName = args[n];
                } else {
                    System.out.println("-map 参数后无地铁路线信息文件，请检查参数。");
                    return;
                }
            } else if (strArg.equals("-a")) {
                n += 1;
                if (n < args.length) {

                    String strLineNum = args[n];
                    if (strLineNum.length() >= 1) {
//                        String strNumber = strLineNum.substring(0, 1);
//                        nLineNum = Integer.parseInt(strNumber);
                    	LineName = strLineNum;
//                        nLineNum = Integer.parseInt(strNumber);
                    	nLineNum = -2;
                    }
                }
            } else if (strArg.equals("-o")) {
                n += 1;
                if (n < args.length) {
                    strOutFileName = args[n];
                } else {
                    System.out.println("-o 参数后无信息输出文件，请检查参数！");
                    return;
                }
            } else if (strArg.equals("-b")) {
                if (n+2 > args.length) {
                    System.out.println("-o 参数后无信息输出文件，请检查参数！");
                    return;
                }

                strStartStationName = args[n+1];
                strEndStationName = args[n+2];
                n += 2;
            } else {
                System.out.println("地址参数不正确，请检查参数！");
                return;
            }
        }
        // ----------------------------------------------------------------
        // 处理地铁地图。
        Map mapSubway = new Map();
          mapSubway.loadLineFile(strSubwayFileName);
        if (nLineNum != -1) {
            if (strOutFileName == null) {
                System.out.println("-o 参数错误");
            }
//            mapSubway.printLineInfo(nLineNum, strOutFileName);
            mapSubway.printLineInfo(LineName, strOutFileName);
        } else if (strStartStationName != null) {
            if (strEndStationName == null || strOutFileName == null) {
                System.out.println("-b 或 -o 参数错误");
            }
            Path Dijkstra = mapSubway.Dijkstra(strEndStationName,strStartStationName);
            if(Dijkstra==null)
            	System.out.print("");
            else {
            System.out.println(strEndStationName);
            mapSubway.printPath(strEndStationName, strStartStationName,strOutFileName);
            }
        }
    }
}


