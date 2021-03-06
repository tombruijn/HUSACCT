package husacct.validate.presentation.browseViolations;

import husacct.ServiceProvider;
import husacct.validate.domain.validation.Severity;
import java.awt.GridLayout;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

public class StatisticsPanel extends JPanel{

	private static final long serialVersionUID = -7741400148880504572L;

	private JLabel totalViolationLabel, totalViolationNumber, shownViolationsLabel, shownViolationsNumber;

	public StatisticsPanel() {
		
		inintComponents();
	}

	private void inintComponents() {
		totalViolationLabel = new JLabel();
		totalViolationNumber = new JLabel("0");
		shownViolationsLabel = new JLabel();
		shownViolationsNumber = new JLabel("0");

		add(totalViolationLabel);
		add(totalViolationNumber);
		add(shownViolationsLabel);
		add(shownViolationsNumber);
		
		createBaseLayout();
	}
	
	private void createBaseLayout(){
		setLayout(new GridLayout(0, 2));
	}
	
	public void loadAfterChange(){
		loadText();
	}
	
	private void loadText() {
		setBorder(new TitledBorder(ServiceProvider.getInstance().getLocaleService().getTranslatedString("Information")));
		totalViolationLabel.setText(ServiceProvider.getInstance().getLocaleService().getTranslatedString("TotalViolations") + ":");
		shownViolationsLabel.setText(ServiceProvider.getInstance().getLocaleService().getTranslatedString("ShownViolations") + ":");
	}
	
	public void loadStatistics(LinkedHashMap<Severity, Integer> severitiesCountPerSeverity, int totalSize, int shownSize){
		removeAll();
		
		totalViolationNumber.setText("" + totalSize);

		shownViolationsNumber.setText("" + shownSize);

		add(totalViolationLabel);
		add(totalViolationNumber);

		add(shownViolationsLabel);
		add(shownViolationsNumber);

		for(Entry<Severity, Integer> violationPerSeverity: severitiesCountPerSeverity.entrySet()){
			add(new JLabel(violationPerSeverity.getKey().toString()));
			add(new JLabel(violationPerSeverity.getValue().toString()));
		}
		
		updateUI();
	}
}
