package com.example.demo.entity;

import com.example.demo.data.Constant;
import com.opencsv.CSVWriter;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;

import static java.lang.Math.*;

public class Field {
    public static final double _APPi = 1.00 * 1.00; // Area per plant (row x interRow spacing) (m2)
    public static final int _nsl = 5; // number of soil layer
    public static final double _lw = 0.9 / _nsl; // depth/_nsl // thickness of a layer (m) _depth = 0.9
    public static final double _lvol = _lw * _APPi; // depth*_APPI/_nsl // volume of one soil layer
    public static final double _BD = 1360; // soild bulk density in (kg/m3) # Burrium 1.36, Ratchaburi 1.07 g.cm3
    public static double _cuttingDryMass = 75.4; // g
    public static double _leafAge = 75;
    public static double _SRL = 39.0; // m/g
    public static double _iStart = 91;
    public static double _iend = 361;
    public static boolean _zerodrain = true;
    // todo needs to be based on planting date provided by user then weather should start at right point
    public static double _iTheta = 0.2;
    public static double _thm = 0.18; //drier todo make
    public static double _ths = 0.27; //field capacity, not saturation todo rename
    public static double _thr = 0.015; // residual water content
    public static double _thg = 0.02;
    public static double _rateFlow = 1.3;
    // order of value weather in list weatherData
    final int _iTime = 0;
    final int _iDOY = 1;
    final int _iRadiation = 2;
    final int _iRain = 4;
    final int _iRH = 5;
    final int _iTemp = 3;
    final int _iWind = 6;
    final int _iIrrigation = 8;
    public static List<List<Object>> _weatherData = new ArrayList<List<Object>>();
    static String fieldName;

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    int dAP;
    String startTime;
    boolean irrigationCheck;
    double amountOfIrrigation;
    List<Double> yields;
    String checkYieldDate;

    public void setCustomized_parameters(CustomizedParameters customized_parameters) {
        this.customized_parameters = customized_parameters;
    }

    public CustomizedParameters getCustomized_parameters() {
        return customized_parameters;
    }

    public CustomizedParameters customized_parameters;
    MeasuredData measuredData;
    String startIrrigation;
    String endIrrigation;
    double _autoIrrigateTime = -1;
    public List<List<Double>> _results = new ArrayList<>();


    public Field(
            String fieldName,
            String startTime,
            int dAP,
            boolean irrigationCheck,
            double amountOfIrrigation,
            List<Double> yields,
            String checkYieldDate,
            CustomizedParameters customized_parameters,
            MeasuredData measuredData,
            String startIrrigation,
            String endIrrigation) {
        this.fieldName = fieldName;
        this.startTime = startTime;
        this.dAP = dAP;
        this.irrigationCheck = irrigationCheck;
        this.amountOfIrrigation = amountOfIrrigation;
        this.yields = yields;
        this.checkYieldDate = checkYieldDate;
        this.customized_parameters = customized_parameters;
        this.measuredData = measuredData;
        this.startIrrigation = startIrrigation;
        this.endIrrigation = endIrrigation;
    }

    public Field(String name) {
        this.fieldName = name;
        this.startTime = String.valueOf(new Date(2023, 10, 20));
        this.dAP = 0;
        this.irrigationCheck = false;
        this.amountOfIrrigation = 0;
        this.yields = new ArrayList<>();
        this.yields.add(0.0);
        this.checkYieldDate = "";
        this.customized_parameters = new CustomizedParameters(name);
        this.measuredData = new MeasuredData(name);
        this.startIrrigation = "";
        this.endIrrigation = "";
    }

    public static double relTheta(double th) {
        return lim((th - _thr) / (_ths - _thr), 0, 1);
    }

    public static double lim(double x, double xl, double xu) {
        if (x > xu) {
            return xu;
        } else if (x < xl) {
            return xl;
        } else {
            return x;
        }
    }

    // convert Date to doy
    public static double getDoy(Date sd) {
        Calendar rsd = Calendar.getInstance();
        rsd.setTime(sd);
        rsd.set(Calendar.MONTH, Calendar.JANUARY);
        rsd.set(Calendar.DAY_OF_MONTH, 1);
        rsd.set(Calendar.HOUR_OF_DAY, 0);
        rsd.set(Calendar.MINUTE, 0);
        rsd.set(Calendar.SECOND, 0);

        double doy = (double) ((sd.getTime() - rsd.getTime().getTime()) / (1000 * 60 * 60 * 24));
        doy += sd.getHours() / 24.0 +
                sd.getMinutes() / (24.0 * 60.0) +
                sd.getSeconds() / (24.0 * 60.0 * 60.0);
        return doy;
    }

    public static List<Double> multiplyLists(List<Double> l1, List<Double> l2) {
        int n = min(l1.size(), l2.size());
        List<Double> result = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            result.add(l1.get(i) * l2.get(i));
        }
        return result;
    }

    public static List<Double> multiplyListsWithConstant(List<Double> l, double c) {
        List<Double> result = new ArrayList<>();
        for (Double number : l) {
            result.add(number * c);
        }
        return result;
    }

    public static double monod(double conc, double Imax, double Km) {
        double pc = Math.max(0.0, conc);
        return pc * Imax / (Km + pc);
    }

    public static double logistic(double x, double x0, double xc, double k, double m) {
        return x0 + (m - x0) / (1 + exp(-k * (x - xc)));
    }

    public static double photoFixMean(double ppfd, double lai,
                                      double kdf, double Pn_max, double phi, double k) {
        double r = 0;
        int n = 30;
        double b = 4 * k * Pn_max;
        for (int i = 0; i < n; ++i) {
            double kf = exp(kdf * lai * (i + 0.5) / n);
            double I = ppfd * kf;
            double x0 = phi * I;
            double x1 = x0 + Pn_max;
            double p = x1 - sqrt(x1 * x1 - b * x0);
            r += p;
        }
        r *= -12e-6 * 60 * 60 * 24 * kdf * _APPi * lai / n / (2 * k);
        return r;
    }

    public static double fSLA(double ct) {
        return logistic(ct, 0.04, 60, 0.1, 0.0264);
    }

    public static double fKroot(double th, double rl) {
        double rth = relTheta(th);
        double kadj = min(1.0, pow(rth / 0.4, 1.5));
        double Ksr = 0.01;
        return Ksr * kadj * rl;
    }

    public static double fWaterStress(double minV, double maxV, double the) {
        double s = 1 / (maxV - minV);
        double i = -1 * minV * s;
        return lim(i + s * relTheta(the), 0, 1);
    }

    public static double getStress(double clab, double dm, double low, double high, boolean swap) {
        if (high < -9999.0) {
            high = low + 0.01;
        }
        double dm1 = Math.max(dm, 0.001);
        double cc = clab / dm1;
        double rr = lim(((cc - low) / (high - low)), 0, 1);

        if (swap) {
            rr = 1.0 - rr;
        }
        return rr;
    }

    public void loadAllWeatherDataFromCsvFile() throws IOException {
        List<List<Object>> weatherData = new ArrayList<>();
        String path = "H:\\demo1\\src\\main\\java\\com\\example\\demo\\data\\dataTestAcess.csv";
        File csvFile = new File(path);

        FileReader fileReader = new FileReader(csvFile);
        CSVParser csvParser = new CSVParser(fileReader, CSVFormat.DEFAULT);

        for (CSVRecord record : csvParser) {
            List<Object> rowData = new ArrayList<>();
            for (String value : record) {
                rowData.add(value);
            }
            weatherData.add(rowData);
        }

        // openweatherData
        for (int i = 0; i < weatherData.size() - 1; i++) {
            String time = weatherData.get(i).get(0).toString();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date;
            try {
                date = dateFormat.parse(time);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
            weatherData.get(i).set(1, getDoy(date));
        }
        _weatherData = weatherData;
//        List<List<Object>> weatherDataTemp = new ArrayList<>();
//        weatherData.get(0).set(7,0.1);
//        weatherDataTemp.add(weatherData.get(0));
//
//        for (int i = 0; i < weatherData.size() - 2; i++) {
//            Double dt = Double.parseDouble(weatherData.get(i + 1).get(1).toString()) -
//                    Double.parseDouble(weatherDataTemp.get(weatherDataTemp.size() - 1).get(1).toString());
//            if ( dt >= 0.01) {
//                weatherData.get(i + 1).set(7,dt);
//                weatherDataTemp.add(weatherData.get(i + 1));
//            }
//        }
        //_weatherData = weatherDataTemp;
        // writeDataCsvFile(_weatherData);
        csvParser.close();
        fileReader.close();
    }

    public double getIrrigationAmount() {
        int length = _results.get(2).size();
        double irr = (length > 1)
                ? _results.get(2).get(length - 1) - _results.get(2).get(length - 2)
                : _results.get(2).get(0);
        return irr * 0.1; // convert from m3/ha to l/m2
    }

    public void runModel() throws IOException {
        loadAllWeatherDataFromCsvFile();
        simulate();
        writeDataCsvNew();
    }

    public void ode2InitModel(Double startTime, Double endTime) {
    }

    public List<Double> ode2initValuesTime0() {
        List<Double> yi = new ArrayList<>();
        for (int index = 0; index < 9 + _nsl * 5; ++index) {
            yi.add(0.0);
        }

        List<Double> iTheta = new ArrayList<>();
        for (int index = 0; index < _nsl; ++index) {
            iTheta.add(_iTheta + index * _thg);
        }

        List<Double> iNcont = new ArrayList<>();
        iNcont.add(39.830);
        iNcont.add(10.105);
        iNcont.add(16.050);
        iNcont.add(8.0);
        iNcont.add(8.0);
        for (int index = 5; index < 15; ++index) {
            iNcont.add(0.0);
        }

        double iNRT = 6.0;
        yi.set(1, _cuttingDryMass);
        yi.set(6, _cuttingDryMass);

        yi.set(9 + _nsl, iNRT);

        for (int i = 0; i < _nsl; ++i) {
            yi.set(9 + 2 * _nsl + i, iTheta.get(i));
            yi.set(9 + 3 * _nsl + i, iNcont.get(i) * this.customized_parameters.fertilizationLevel / 100);
            yi.set(9 + 4 * _nsl + i, _cuttingDryMass * 30.0 / _nsl);
        }

        yi.add(0.0);
        yi.add(0.0);
        yi.add(0.0);
        yi.add(0.0);
        yi.add(0.0);
        yi.add(0.0);
        yi.add(0.0);

        return yi;
    }

    private void writeDataCsvNew() {
        String csvFilePath = "resultWeatherThaiLan.csv";
        try {
            FileWriter writer = new FileWriter(csvFilePath);
            CSVWriter csvWriter = new CSVWriter(writer);
            List<String[]> data = new ArrayList<>();
            data.add(new String[]{"day", "Yeild", "irr", "LeafArea", "LabieCarbon"});
            for (int i = 1; i < _results.get(0).size(); i++) {
                data.add(new String[]{String.valueOf(LocalDate.ofYearDay(2023, (int) Math.ceil(_results.get(8).get(i)))), String.valueOf(_results.get(0).get(i)), String.valueOf(_results.get(2).get(i)), String.valueOf(_results.get(3).get(i)), String.valueOf(_results.get(4).get(i))});
            }
            csvWriter.writeAll(data);
            csvWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeDataToCsvFile() {
        String csvFilePath = "irrigation_data1.csv";
        try {
            FileWriter writer = new FileWriter(csvFilePath);
            CSVWriter csvWriter = new CSVWriter(writer);
            List<String[]> data = new ArrayList<>();
            data.add(new String[]{"day", "Yeild", "irr"});
            for (int i = 0; i < _results.get(0).size(); i++) {
                data.add(new String[]{String.valueOf(i), String.valueOf(_results.get(0).get(i)), String.valueOf(_results.get(2).get(i))});
            }
            csvWriter.writeAll(data);
            csvWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // calulator models
//    public void simulate() {
//        _iStart = Double.parseDouble(_weatherData.get(1).get(_iDOY).toString());
//        _iend = Double.parseDouble(_weatherData.get(_weatherData.size() - 1).get(_iDOY).toString());
//        _autoIrrigateTime = -1;
//        //initialize to start simulate
//        List<Double> w = ode2initValues();
//        // _results = new ArrayList<>();
//        for (int i = 0; i < 10; i++) {
//            _results.add(new ArrayList<>());
//        }
//        for (int i = 2; i < _weatherData.size() - 1; i++) {
//            if (Double.parseDouble(_weatherData.get(i).get(1).toString()) > 277)
//                continue;
//            //get weather data
//            List<Double> wd = new ArrayList<>(); //weatherData
//            double rain = Double.parseDouble(_weatherData.get(i).get(_iRain).toString());
//            wd.add(rain); //wd[0]
//            double tempC = Double.parseDouble(_weatherData.get(i).get(_iTemp).toString());
//            wd.add(tempC); //wd[1]
//            double radiation = 24 * Double.parseDouble(_weatherData.get(i).get(_iRadiation).toString());
//            wd.add(2.5 * radiation); // ppfd = 2.5 * radiation, wd[2], photosynthetic photon flux density
//            double relativeHumidity = Double.parseDouble(_weatherData.get(i).get(_iRH).toString());
//            double wind = Double.parseDouble(_weatherData.get(i).get(_iWind).toString());
//            double doy = Double.parseDouble(_weatherData.get(i).get(1).toString());
//            double et0 = hourlyET(
//                    tempC,
//                    radiation,
//                    relativeHumidity,
//                    wind,
//                    doy,
//                    Constant.latitude,
//                    Constant.longitude,
//                    Constant.elevation,
//                    Constant.longitude,
//                    Constant.height);
//            wd.add(et0); //wd[3]
//
//            wd.add(0.0); //for irrigation,wd[4]
//            //khoảng cách thời gian giữa 2 thời gian
//            double dt = doy == _iStart ? 1e-10 : (Double.parseDouble(_weatherData.get(i).get(_iDOY).toString()) - Double.parseDouble(_weatherData.get(i - 1).get(_iDOY).toString()));
//            wd.add(dt);
//            // wd.set(0, rain*24);
//            //do step
//            rk4Step(doy - 76, w, dt, wd);
//
//            //if the next time is in a next day
//            if ((Math.floor(Double.parseDouble(_weatherData.get(i + 1).get(_iDOY).toString())) - Math.floor(doy)) > 0) {
//                _results.get(0).add(w.get(3) * 10 / _APPi); //yield
//                _results.get(1).add(w.get(9 + 2 * _nsl)); //theta
//                _results.get(2).add(w.get(9 + 5 * _nsl)); //irrigation;
//
//                _results.get(3).add(w.get(4) / _APPi); //lai
//                _results.get(4).add(100.0 + 100.0 * w.get(8) / Math.max(1.0, w.get(0) + w.get(1) + w.get(2) + w.get(3))); //clab
//                _results.get(5).add(w.get(9 + 5 * _nsl + 5)); //photo
//                _results.get(6).add(w.get(9 + 3 * _nsl)); //topsoil ncont
//                int ri = 9 + 4 * _nsl;
//                final double Nopt = 45 * w.get(0) + 2 * w.get(3) + 20 * w.get(1) + 20 * w.get(2);
//                _results.get(7).add((w.subList(ri, ri + _nsl))
//                        .stream()
//                        .reduce((value, element) -> value + element)
//                        .orElse(0.0) / Math.max(1.0, Nopt)); //nupt
//                _results.get(8).add(doy); //doy
//                _results.get(9).add(wd.get(3));
//            }
//        }
//        writeDataCsvFile(_results);
//    }
    int _iwdRowNum = 1;
    final int _iDT = 2; //13;
    final int _iLat = 7; //5;
    final int _iLong = 8; //6;
    final int _iElev = 9; //7;
    final int _iHeight = 10; //4;

    //lấy giá trị có thời gian gần với t nhất trong mảng _weatherData
    public List<Double> getWeatherData(double t) {
        // Reference day for the first entry in weather data
        double doy = Double.parseDouble(_weatherData.get(_iwdRowNum).get(_iDOY).toString());
        double doyn = (doy + Double.parseDouble(_weatherData.get(_iwdRowNum).get(_iDT).toString()));

        while (t > doyn && _iwdRowNum < _weatherData.size()) {
            ++_iwdRowNum;
            doy = Double.parseDouble(_weatherData.get(_iwdRowNum).get(_iDOY).toString());
            doyn = doy + Double.parseDouble(_weatherData.get(_iwdRowNum).get(_iDT).toString());
        }

        while (t < doy + 1e-9 && _iwdRowNum > 1) {
            --_iwdRowNum;
            doy = Double.parseDouble(_weatherData.get(_iwdRowNum).get(_iDOY).toString());
            // doyn = doy + _weatherData[_iwdRowNum][_iDT].toDouble();
        }

        final int n = _iwdRowNum;
        assert abs(t - doy) < 0.1;

        // Update weather

        double dt = Double.parseDouble(_weatherData.get(n).get(_iDT).toString());
        double rain = Double.parseDouble(_weatherData.get(n).get(_iRain).toString())*24; // mm to mm/day
        double temp = Double.parseDouble(_weatherData.get(n).get(_iTemp).toString());
        double radiation = Double.parseDouble(_weatherData.get(n).get(_iRadiation).toString());
        double relativeHumidity = Double.parseDouble(_weatherData.get(n).get(_iRH).toString());
        double wind = Double.parseDouble(_weatherData.get(n).get(_iWind).toString());
        double latitude = 21.0075;
        double longitude =105.5416;
        double elevation = 16;
        double height = 2.5;
        double ppfd = radiation * 2.15; // 2.15 for conversion of energy to ppfd

        // double et0 = row.get(_iET0).doubleValue(); // this is not the reference ET, but already corrected we recalculate
        double et0 = 24 * hourlyET(temp, radiation, relativeHumidity, wind, doy, latitude, longitude, elevation, longitude, height);
        double irri = 0; // TODO: Allow the farmer to enter // row.get(_iIrrigation).doubleValue();

        List<Double> YR = new ArrayList<>();
        YR.add(rain);
        YR.add(temp);
        YR.add(ppfd);
        YR.add(et0);
        YR.add(irri);
        YR.add(dt);
        YR.add(Double.valueOf(n));

        for (Double e : YR) {
            assert !Double.isNaN(e);
        }

        return YR;
    }

    int pdt = 1;
    int _printSize = 366;
    List<Double> _printTime = new ArrayList<>(Collections.nCopies(_printSize, -1000.0));

//    public void setDonViThaiLan() {
//        // Doy,Rain,dt,Temp,Radiation,Relative Humidity,Wind,lat,long,elev
//        _iDOY = 0;
//        _iRadiation = 4;
//        _iRain = 1;
//        _iRH = 5;
//        _iTemp = 3;
//        _iWind = 6;
//    }

    public void simulate() {
        _iStart = Double.parseDouble(_weatherData.get(1).get(_iDOY).toString());
        _iend = Double.parseDouble(_weatherData.get(_weatherData.size() - 2).get(_iDOY).toString());
        _autoIrrigateTime = -1;
        double t = _iStart;
        List<Double> w = ode2initValuesTime0(); //initialize to start simulate
        double dt = (double) 60 / (60 * 24);
        int ps = min(_printSize, (int) ceil((_iend - _iStart) / pdt));
        List<Double> ptime = new ArrayList<>();
        for (int index = 0; index < ps; ++index) {
            ptime.add(_iStart + (double) index * pdt);
        }
        for (int i = 0; i <= 8; i++) {
            List<Double> innerList = new ArrayList<>(Collections.nCopies(ptime.size(), 0.0));
            _results.add(innerList);
        }
        t = _iStart;
        for (int i = 0; i < ptime.size(); ++i) {
            // Forward simulation
            List<Double> wd = getWeatherData(t);
            double tw = t + wd.get(5);
            while (t < ptime.get(i) - 0.5 * dt) {
                double wddt = max(1e-10, min(min(dt, wd.get(5)), ptime.get(i) - t));
                // Do step
                rk4Step(t - _iStart, w, wddt, wd);
                t += wddt;

                // Next row in weather data
                if (t > tw) {
                    wd = getWeatherData(t);
                    tw = t + wd.get(5);
                }
            }

            // Populate results
            _printTime.set(i, t);
            _results.get(0).set(i, w.get(3) * 10 / _APPi);
            _results.get(1).set(i, w.get(9 + 2 * _nsl));
            _results.get(2).set(i, w.get(9 + 5 * _nsl));
            _results.get(3).set(i, w.get(4) / _APPi);
            _results.get(4).set(i, 100.0 + 100.0 * w.get(8) / max(1.0, w.get(0) + w.get(1) + w.get(2) + w.get(3)));
            _results.get(5).set(i, w.get(9 + 5 * _nsl + 5));
            _results.get(6).set(i, w.get(9 + 3 * _nsl));
            // Thêm thời gian
            _results.get(8).set(i, t);

            int ri = 9 + 4 * _nsl;
            final double Nopt = 45 * w.get(0) + 2 * w.get(3) + 20 * w.get(1) + 20 * w.get(2);
            _results.get(7).add((w.subList(ri, ri + _nsl))
                    .stream()
                    .reduce((value, element) -> value + element)
                    .orElse(0.0) / Math.max(1.0, Nopt)); //nupt
        }
        writeDataToCsvFile();
    }

    public void rk4Step(double t, List<Double> y, double dt, List<Double> wd) {
        List<Double> yp = new ArrayList<>(y);

        List<Double> r1 = ode2(t, yp, wd);
        double t1 = t + 0.5 * dt;
        double t2 = t + dt;

        intStep(yp, r1, 0.5 * dt);
        List<Double> r2 = ode2(t1, yp, wd);

        for (int i = 0; i < y.size(); i++) {
            yp.set(i, y.get(i)); // reset
        }

        intStep(yp, r2, 0.5 * dt);
        List<Double> r3 = ode2(t1, yp, wd);

        for (int i = 0; i < y.size(); i++) {
            yp.set(i, y.get(i)); // reset
        }

        intStep(yp, r3, dt);
        List<Double> r4 = ode2(t2, yp, wd);

        for (int i = 0; i < r4.size(); i++) {
            r4.set(i, (r1.get(i) + 2 * (r2.get(i) + r3.get(i)) + r4.get(i)) / 6); // rk4
        }

        intStep(y, r4, dt); // final integration
    }

    public List<Double> ode2(double ct, List<Double> y, List<Double> wd) {
        int cnt = -1;
        double LDM = y.get(++cnt); // Leaf Dry Mass (g)
        double SDM = y.get(++cnt); // Stem Dry Mass (g)
        double RDM = y.get(++cnt); // Root Dry Mass (g)
        double SRDM = y.get(++cnt); // Storage Root Dry Mass (g)
        double LA = y.get(++cnt); // Leaf Area (m2)

        double mDMl = y.get(++cnt); //intgrl("mDMl", 0, "mGRl");
        //double mDMld = y.get(7);//intgrl("mDMld", 0, "mGRld");
        double mDMs = y.get(++cnt); //intgrl("mDMs", cuttingDryMass, "mGRs");
        //double mDM = y.get(9);//intgrl("mDM", 0, "mGR");
        ++cnt; //double mDMsr = y.get(++cnt); //intgrl("mDMsr", 0, "mGRsr");
        //double TR = intgrl("TR", 0, "RR"); // Total Respiration (g C)
        double Clab = y.get(++cnt); // labile carbon pool
        ++cnt;
        List<Double> rlL = y.subList(cnt, cnt += _nsl); //Root length per layer (m)
        //double RL = sumList(RL_l); // Root length (m)

        List<Double> nrtL = y.subList(cnt, cnt += _nsl); //Root tips per layer
        double NRT = 0;
        for (double element : nrtL) {
            NRT += element;
        }
        List<Double> thetaL = y.subList(cnt, cnt += _nsl); //volumetric soil water content for each layer

        //double Ncont_l = intgrl("Ncont",[4.83+35, 10.105, 16.05]*_lvol*BD,"NcontR");// N-content in a soil layer (mg);
        List<Double> ncontL = y.subList(cnt, cnt += _nsl);
        List<Double> nuptL = y.subList(cnt, cnt += _nsl);
        double Nupt = 0;
        for (double element : nuptL) {
            Nupt += element;
        }


        double TDM = LDM + SDM + RDM + SRDM + Clab;
        double cDm = 0.43;
        double leafTemp = wd.get(1);
        double TSphot = lim((-0.832097717 + 0.124485738 * leafTemp - 0.002114081 * Math.pow(leafTemp, 2)), 0, 1);
        double TSshoot = lim((-1.5 + 0.125 * leafTemp), 0, 1) * lim((7.4 - 0.2 * leafTemp), 0, 1);
        double TSroot = 1.0;

        List<Double> krootL = new ArrayList<>();
        for (int i = 0; i < _nsl; ++i) {
            krootL.add(fKroot(thetaL.get(i), rlL.get(i)));
        }
        //sums up all elements.
        double Kroot = krootL.stream().mapToDouble(Double::doubleValue).sum();
        Kroot = Math.max(1e-8, Kroot);

        double thEquiv;
        if (Kroot > 1e-8) {
            double sumThetaKroot = 0.0;
            for (int i = 0; i < _nsl; ++i) {
                sumThetaKroot += thetaL.get(i) * krootL.get(i);
            }
            thEquiv = sumThetaKroot / Kroot;
        } else {
            thEquiv = thetaL.get(0);
        }

        double WStrans = fWstress(0.05, 0.5, thEquiv);
        double WSphot = fWstress(0.05, 0.3, thEquiv);
        double WSshoot = fWstress(0.2, 0.55, thEquiv);
        double WSroot = 1;
        double WSleafSenescence = 1.0 - fWstress(0.0, 0.2, thEquiv);

        // water in soil
        //irrigation either not (rained), or from file, or auto.
        // file/auto should switch on current date?
        double irrigation = this.customized_parameters.autoIrrigation ? wd.get(4) : 0.0;


        double _fcThreshHold = this.customized_parameters.fieldCapacity;
        _fcThreshHold *= (_ths - _thr) / 100;
        _fcThreshHold += _thr;
        // cacular auto Irrigation Duration
        double _autoIrrigationDuration = this.customized_parameters.irrigationDuration / 24;
        //convert from hour to day
        double dhr = this.customized_parameters.dripRate;// l/hour
        double dhd = this.customized_parameters.distanceBetweenHole;//cm
        double dld = this.customized_parameters.distanceBetweenRow;//cm
        double _autoIrrigate = dhr * 24.0 / (dhd * dld / 10000.0);//m3/day

        // khi luong nuoc tuoi nho hon 1e-6
        if (irrigation < 1e-6 &&
                _autoIrrigateTime < ct + _autoIrrigationDuration &&
                _fcThreshHold > _thr &&
                thEquiv < _fcThreshHold) {
            _autoIrrigateTime = ct;
        }
        if (ct < _autoIrrigateTime + _autoIrrigationDuration) {
            irrigation += _autoIrrigate;
        }

        double precipitation = this.customized_parameters.scaleRain / 100 * wd.get(0) + irrigation;

        double ET0reference = wd.get(3);
        double ETrainFactor = (precipitation > 0) ? 1 : 0;
        double kdf = -0.47;
        double ll = Math.exp(kdf * LA / _APPi);
        double cropFactor = Math.max(1 - ll * 0.8, ETrainFactor);
        double transpiration = cropFactor * ET0reference;
        double swfe = Math.pow(relTheta(thetaL.get(0)), 2.5);
        double actFactor = Math.max(ll * swfe, ETrainFactor);
        double evaporation = actFactor * ET0reference;

        double actualTranspiration = transpiration * WStrans;
        List<Double> wuptrL = multiplyListsWithConstant(krootL, actualTranspiration / Kroot);

        double drain = 0.0;
        List<Double> qFlow = new ArrayList<>(Collections.nCopies(_nsl + 1, 0.0));
        qFlow.set(0, (precipitation - evaporation) / (_lw * 1000.0));

        for (int i = 1; i < qFlow.size(); ++i) {
            double thdown = (i < _nsl)
                    ? thetaL.get(i)
                    : (_zerodrain)
                    ? thetaL.get(i - 1) + _thg
                    : _thm;
            qFlow.set(i, qFlow.get(i) +
                    (thetaL.get(i - 1) + _thg - thdown) * _rateFlow * (thetaL.get(i - 1) / _ths) +
                    4.0 * Math.max(thetaL.get(i - 1) - _ths, 0));
        }

        List<Double> dThetaDt = new ArrayList<>();
        for (int i = 0; i < _nsl; ++i) {
            double dTheta = qFlow.get(i) - qFlow.get(i + 1) - wuptrL.get(i) / (_lw * 1000.0);
            dThetaDt.add(dTheta);
            if (Double.isNaN(dTheta)) {
                System.out.println("dThetaDt: " + dTheta + " qFlow: " + qFlow);
            }
        }

        drain = qFlow.get(_nsl) * _lw * 1000;
        // nutrient concentrations in the plant
        double Nopt = 45 * LDM + 7 * SRDM + 20 * SDM + 20 * RDM;
        double NuptLimiter = 1.0 - fNSstress(Nupt, 2.0 * Nopt, 3.0 * Nopt);
        List<Double> nuptrL = new ArrayList<>();
        for (int i = 0; i < _nsl; i++) {
            double nuptr = monod(ncontL.get(i) * _BD / (1000 * thetaL.get(i)),
                    NuptLimiter * rlL.get(i) * 0.8,
                    12.0 * 0.5);
            nuptrL.add(nuptr);
            if (Double.isNaN(nuptr)) {
                System.out.println("ncont_l=" + ncontL + " theta_l=" + thetaL);
            }
        }

        List<Double> ncontrL = new ArrayList<>(Collections.nCopies(_nsl, 0.0));
        List<Double> _NminR_l = new ArrayList<>();
        for (int d = 0; d < _nsl; d++) {
            double nminR = customized_parameters.fertilizationLevel / 100.0 *
                    36.0 / (_lvol * _BD) /
                    Math.pow(d + 1, 2);
            _NminR_l.add(nminR);
        }

        for (int i = 0; i < _nsl; i++) {
            ncontrL.set(i, _NminR_l.get(i));
            ncontrL.set(i, ncontrL.get(i) - nuptrL.get(i) / (_BD * _lvol)); //mg/day/ (m3*kg/m3)
            double Nl = ncontL.get(i);
            double Nu = (i > 0) ? ncontL.get(i - 1) : -ncontL.get(i);
            double Nd = (i < (_nsl - 1)) ? ncontL.get(i + 1) : 0.0;
            ncontrL.set(i, ncontrL.get(i) + qFlow.get(i) * (Nu + Nl) / 2.0 - qFlow.get(i + 1) * (Nl + Nd) / 2.0);
        }

        double NSphot = (Nopt > 1e-3) ? fNSstress(Nupt, 0.7 * Nopt, Nopt) : 1.0;
        double NSshoot = (Nopt > 1e-3) ? fNSstress(Nupt, 0.7 * Nopt, 0.9 * Nopt) : 1.0;
        double NSroot = (Nopt > 1e-3) ? fNSstress(Nupt, 0.5 * Nopt, 0.7 * Nopt) : 1.0;
        double NSleafSenescence = (Nopt > 1.0) ? 1.0 - fNSstress(Nupt, 0.8 * Nopt, Nopt) : 0.0;

        // sink strength
        double mGRl = logistic(ct, 0.3, 70, 0, 0.9);
        double mGRld = logistic(ct, 0.0, 70.0 + _leafAge, 0.1, -0.90);
        double mGRs = logistic(ct, 0.2, 95, 0.219, 1.87) +
                logistic(ct, 0.0, 209, 0.219, 1.87 - 0.84);
        double mGRr = 0.02 + (0.2 + Math.exp(-0.8 * ct - 0.2)) * mGRl;
        double mGRsr = Math.min(7.08, Math.pow(Math.max(0.0, (ct - 32.3) * 0.02176), 2));
        double mDMr = 0.02 * ct + 1.25 + 0.25 * ct -
                1.25 * Math.exp(-0.8 * ct) * mGRl +
                (0.25 + Math.exp(-0.8 * ct)) * mDMl;

        double CSphot = getStress(Clab, TDM, 0.05, -9999.9, true);
        double CSshoota = getStress(Clab, TDM, -0.05, -9999.9, false);
        double CSshootl = lim((5 - LA / _APPi), 0, 1);
        double CSshoot = CSshoota * CSshootl;
        double CSroot = getStress(Clab, TDM, -0.03, -9999.9, false);
        double CSsrootl = getStress(Clab, TDM, -0.0, -9999.9, false);
        double CSsrooth = getStress(Clab, TDM, 0.01, 0.20, false);
        double starchRealloc = getStress(Clab, TDM, -0.2, -0.1, true) * -0.05 * SRDM;
        double CSsroot = CSsrootl + 2 * CSsrooth;
        double SFleaf = WSshoot * NSshoot * TSshoot * CSshootl;
        double SFstem = WSshoot * NSshoot * TSshoot * CSshoot;
        double SFroot = WSroot * NSroot * TSroot * CSroot;
        double SFsroot = CSsroot;

        double CsinkL = cDm * mGRl * SFleaf;
        double CsinkS = cDm * mGRs * SFstem;
        double CsinkR = cDm * mGRr * SFroot;
        double CsinkSR = cDm * mGRsr * SFsroot - starchRealloc;
        double Csink = CsinkL + CsinkS + CsinkR + CsinkSR;

        // biomass partitioning
        double a2l = CsinkL / Math.max(1e-10, Csink);
        double a2s = CsinkS / Math.max(1e-10, Csink);
        double a2r = CsinkR / Math.max(1e-10, Csink);
        double a2sr = CsinkSR / Math.max(1e-10, Csink);

// carbon to growth
        double CFG = Csink;// carbon needed for growth (g C/day)
        // increase in plant dry Mass (g DM/day) not including labile carbon pool
        double IDM = Csink / cDm;
        double PPFD = wd.get(2);
        double SFphot = Math.min(Math.min(TSphot, WSphot), Math.min(NSphot, CSphot));
        double CFR = photoFixMean(PPFD, LA / _APPi, -0.47, 29.37 * SFphot, 0.05553, 0.90516);
//photosynthesis
        double SDMR = a2s * IDM;
        double SRDMR = a2sr * IDM;
        double SLA = fSLA(ct);
        double LDRstress = WSleafSenescence * NSleafSenescence * LDM * -1.0;
        double LDRage = mGRld * ((mDMl > 0) ? LDM / mDMl : 1.0);
        if (LDRstress > 1e-10 || LDRage > 1e-10) {
            throw new AssertionError("LDRstress: " + LDRstress + " LDRage: " + LDRage);
        }
        double LDRm = Math.max(-LDM, LDRstress + LDRage);
        double LDRa = Math.max(-LA, fSLA(Math.max(0.0, ct - _leafAge)) * LDRm);
        double LAeR = SLA * a2l * IDM + LDRa;// Leaf Area expansion Rate (m2/day)
        double LDMR = a2l * IDM + LDRm;// leaf growth rate (g/day) - death rate (g/day)

        double RDMR = a2r * IDM; // fine root growth rate (g/day)
        double RLR = _SRL * RDMR;
        List<Double> rlrL = new ArrayList<>();
        for (int i = 0; i < _nsl; ++i) {
            double ln1 = RLR * nrtL.get(i) / NRT;
            rlrL.add(ln1);
        }
        double ln0 = 0.0;
        List<Double> nrtrL = new ArrayList<>();
        for (int i = 0; i < _nsl; ++i) {
            double ln1 = rlrL.get(i);
            nrtrL.add(ln1 * 60.0 + Math.max(0, (ln0 - ln1 - 6.0 * _lw) * 10.0 / _lw));
            ln0 = ln1;
        }

        double mRR = 0.003 * RDM + 0.0002 * SRDM + 0.003 * LDM + 0.0002 * SDM;
        double gRR = 1.8 * RDMR + 0.2 * SRDMR + 1.8 * (LDMR - LDRm) + 0.4 * SDMR;
        double RR = mRR + gRR;

        double ClabR = (CFR - CFG - RR) / cDm;
        cnt = -1;
        List<Double> YR = new ArrayList<>();
        YR.add(++cnt, LDMR);
        YR.add(++cnt, SDMR);
        YR.add(++cnt, RDMR);
        YR.add(++cnt, SRDMR);
        YR.add(++cnt, LAeR);
        YR.add(++cnt, mGRl);
        YR.add(++cnt, mGRs);
        YR.add(++cnt, (double) mGRsr); // Using (double) to convert to double
        YR.add(++cnt, ClabR);
        YR.addAll(rlrL);
        YR.addAll(nrtrL);
        YR.addAll(dThetaDt);
        YR.addAll(ncontrL);
        YR.addAll(nuptrL);

        YR.add((double) irrigation); // Just for reporting amount of water needed
        YR.add(wd.get(0)); // rain
        YR.add((double) actualTranspiration); // Just for reporting amount of water needed
        YR.add(evaporation);
        YR.add(drain);
        YR.add(CFR);
        YR.add(PPFD);

        return YR;
    }

    public double fWstress(double minv, double maxv, double the) {
        double s = 1 / (maxv - minv);
        double i = -1 * minv * s;
        return lim((i + s * relTheta(the)), 0, 1);
    }


    public double fNSstress(double upt, double low, double high) {
        double rr = (upt - low) / (high - low);
        return lim(rr, 0, 1);
    }

    public List<Double> ode2initValues() {
        List<Double> yi = new ArrayList<>();
        // Dữ liệu mới
        Double[] dataToAdd = {
                59.9936, 60.6656, 18.267223323508567, 93.0756,
                2.4665560462821263, 91.79400000000325, 202.84134540781776, 277.46639346799145,
                31.685022446633873, 246.0908445789579, 206.014020579492, 150.50254098663453,
                84.81732279105968, 24.99698068055664, 14771.450674737114, 12376.546166518712,
                9053.470870979236, 5109.872440430796, 1509.2600113651777, 0.2381801339197489,
                0.23832060394348634, 0.2528704670241301, 0.27385732934211704, 0.2953060649096794,
                0.5699703070593461, 0.45975876345694205, 3.0590914791309287, 1.0999841454865997,
                6.4442547370882055, 7888.635585812703, 6728.453739220788, 5256.491465187306,
                2797.305390900457, 1209.871412709246, 0.0, 258.30194219999834,
                440.58641444762077, 197.81995900125494, 55.96304865702387, 590.9555448821034,
                44512.398073766424
        };

        for (Double value : dataToAdd) {
            yi.add(value);
        }
        return yi;
    }


    public double hourlyET(
            final double tempC,
            final double radiation,
            final double relativeHumidity,
            final double wind,
            final double doy,
            final double latitude,
            final double longitude,
            final double elevation,
            final double longZ,
            final double height) {

        final double pi = Math.PI;
        final double hours = (doy % 1) * 24;
        final double tempK = tempC + 273.16;

        final double Rs = radiation * 3600 / 1e+06;
        final double P = 101.3 *
                Math.pow((293 - 0.0065 * elevation) / 293, 5.256);
        final double psi = 0.000665 * P;

        final double Delta = 2503 *
                Math.exp((17.27 * tempC) / (tempC + 237.3)) /
                Math.pow(tempC + 237.3, 2);
        final double eaSat = 0.61078 *
                Math.exp((17.269 * tempC) / (tempC + 237.3));
        final double ea = (relativeHumidity / 100) * eaSat;

        final double DPV = eaSat - ea;
        final double dr = 1 + 0.033 * Math.cos(2 * pi * doy / 365.0);
        final double delta = 0.409 *
                Math.sin(2 * pi * doy / 365.0 - 1.39);
        final double phi = latitude * (pi / 180);
        final double b = 2.0 * pi * (doy - 81.0) / 364.0;

        final double Sc = 0.1645 * Math.sin(2 * b) - 0.1255 * Math.cos(b) - 0.025 * Math.sin(b);
        final double hourAngle = (pi / 12) *
                ((hours + 0.06667 * (longitude * pi / 180.0 - longZ * pi / 180.0) + Sc) - 12.0);
        final double w1 = hourAngle - ((pi) / 24);
        final double w2 = hourAngle + ((pi) / 24);
        final double hourAngleS = Math.acos(-Math.tan(phi) * Math.tan(delta));
        final double w1c = (w1 < -hourAngleS) ? -hourAngleS : (w1 > hourAngleS) ? hourAngleS : (w1 > w2) ? w2 : w1;
        final double w2c = (w2 < -hourAngleS) ? -hourAngleS : (w2 > hourAngleS) ? hourAngleS : w2;

        final double Beta = Math.asin((Math.sin(phi) * Math.sin(delta) + Math.cos(phi) * Math.cos(delta) * Math.cos(hourAngle)));

        final double Ra = (Beta <= 0) ? 1e-45 : ((12 / pi) * 4.92 * dr) *
                (((w2c - w1c) * Math.sin(phi) * Math.sin(delta)) +
                        (Math.cos(phi) * Math.cos(delta) * (Math.sin(w2) - Math.sin(w1))));

        final double Rso = (0.75 + 2e-05 * elevation) * Ra;

        final double RsRso = (Rs / Rso <= 0.3) ? 0.0 : (Rs / Rso >= 1) ? 1.0 : Rs / Rso;
        final double fcd = (1.35 * RsRso - 0.35 <= 0.05) ? 0.05 : (1.35 * RsRso - 0.35 < 1) ? 1.35 * RsRso - 0.35 : 1;

        final double Rna = ((1 - 0.23) * Rs) -
                (2.042e-10 * fcd * (0.34 - 0.14 * Math.sqrt(ea)) * Math.pow(tempK, 4));

        final double Ghr = (Rna > 0) ? 0.04 : 0.2;
        // G for hourly depend on Rna (or Rn in EThourly)
        final double Gday = Rna * Ghr;
        final double wind2 = wind * (4.87 / (Math.log(67.8 * height - 5.42)));
        final double windf = (radiation > 1e-6) ? 0.25 : 1.7;

        final double EThourly = ((0.408 * Delta * (Rna - Gday)) +
                (psi * (66 / tempK) * wind2 * (DPV))) /
                (Delta + (psi * (1 + (windf * wind2))));

        return EThourly;
    }

    void intStep(final List<Double> y, final List<Double> r, final double dt) {
        assert (y.size() == r.size());
        for (int i = 0; i < y.size(); ++i) {
            y.set(i, y.get(i) + dt * r.get(i));
        }
    }
}
