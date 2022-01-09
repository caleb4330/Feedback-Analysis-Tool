import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

class feature_data {
	String feature;
	int frequency = 0;
	int frequency_positive = 0;
	int frequency_negative = 0;
	int frequency_netural = 0; //contain both positive and negative or none
}

class feature extends feature_data {
	
	//
	void check_line(List<String> feedback, String[] positive_words, String[] negative_words) {
		//check if line contains the feature
		boolean contains_feautre = false;
		boolean contains_positive = false;
		boolean contains_negative = false;
		for (String line : feedback) {
			if (line.contains(this.feature) && !contains_feautre) {
				contains_feautre = true;
				this.frequency++;
			}
			for (String word : positive_words) {
				if (line.contains(word) && !contains_positive) {
					contains_positive = true;
				}	
			}
			for (String word : negative_words) {
				if (line.contains(word) && !contains_positive) {
					contains_negative = true;
				}	
			}
		}
		if (contains_feautre) {
			
		if (contains_positive==contains_negative) {
			this.frequency_netural++;
		}
		else {
			if (contains_positive) {
				this.frequency_positive++;
			}
			else {
				this.frequency_negative++;
			}
		}
		}
	}
}

class feedback_report {
	List<feature> features;
	int num_of_feedback = 0;
}



public class Producer_Consumer_Multithreading {

    public static void main(String[] args) throws InterruptedException {
        BlockingQueue<String> blockingQueue = new LinkedBlockingDeque<>(2);
        

        Thread producerThread = new Thread(() -> {
            try {
                String value;
    			
    			//the file to be opened for reading  
    			FileInputStream fis=new FileInputStream("test.txt");       
    			Scanner sc=new Scanner(fis); 
                
                
                while (sc.hasNextLine()) {
                	String line = sc.nextLine();
                	blockingQueue.put(line);
                }
                //done
                String done = "DONE";
                blockingQueue.put(done);
            } catch (InterruptedException | FileNotFoundException e) {
                e.printStackTrace();
            }
       });

        Thread consumerThread = new Thread(() -> {
            try {
            	String[] positive_words = {"good", "great", "favorite", "amazing", "delicious", "awesome"};
            	String[] negative_words = {"bad", "not good", "gross", "disapointing", "sad", "insufficient"};
            	List<feature> features = new ArrayList<feature>();
            	System.out.println("Enter features which you want to check frequency of (seperate by ', '): ");
            	Scanner input = new Scanner(System.in);
            	String keywords = input.nextLine();
            	String[] strfeatures = keywords.split(", ");
            	for (String feature_name: strfeatures) {
            		feature feature = new feature();
            		feature.feature = feature_name;
            		features.add(feature);
            	}
            	System.out.println();
            	
            	System.out.print("Our key positive feeback trigger words are: ");
            	for (String word : positive_words) {
            		System.out.print(word + ", ");
            	}
            	System.out.println();
            	System.out.println();
            	
            	System.out.print("Our key negative feeback trigger words are: ");
            	for (String word : negative_words) {
            		System.out.print(word + ", ");
            	}
            	System.out.println();
            	System.out.println();
            	
            	System.out.print("Our features for the feedback analysis are: ");
            	for (feature word : features) {
            		System.out.print(word.feature + ", ");
            	}
            	System.out.println();
            	System.out.println();
            	
            	
            	List<String> feedback = new ArrayList<String>();
            	feedback_report report = new feedback_report();
            	report.features = features;
                while (true) {
                    String value = blockingQueue.take();
                    feedback.add(value);
                    if (value.equals("DONE")) {
                    	//run analysis
                    	report.num_of_feedback++;
                    	for (feature feature: report.features) {
                    		feature.check_line(feedback, positive_words, negative_words);
                    	}
                    	break;
                    }
                    if (value.isEmpty()) {
                    	//run analysis
                    	report.num_of_feedback++;
                    	for (feature feature: report.features) {
                    		feature.check_line(feedback, positive_words, negative_words);
                    	}
                    	feedback.clear();
                    }
                    feedback.add(value);

                }
               //generating report
                
                File fout = new File("output.txt");
            	FileOutputStream fos = new FileOutputStream(fout);
             
            	BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
            	bw.write("Total amount of feedback comments: " + report.num_of_feedback);
            	bw.newLine();
            	bw.newLine();
            	for (feature feature: report.features) {
            		bw.write(feature.feature + ": ");
            		bw.newLine();
            		bw.write("frequency: " + feature.frequency);
            		bw.newLine();
            		bw.write("positive frequency: " + feature.frequency_positive);
            		bw.newLine();
            		bw.write("negative frequency: " + feature.frequency_negative);
            		bw.newLine();
            		bw.write("neutral frequency: " + feature.frequency_netural);
            		bw.newLine();
            		bw.newLine();
            	}
            	bw.close();
            	

            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        });

        producerThread.start();
        consumerThread.start();

        producerThread.join();
        consumerThread.join();
    }
}