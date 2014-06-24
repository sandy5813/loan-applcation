package lv.iljakorneckis.webloans.service;

import lv.iljakorneckis.webloans.component.LoanRiskAssessor;
import lv.iljakorneckis.webloans.domain.Loan;
import lv.iljakorneckis.webloans.domain.LoanApplication;
import lv.iljakorneckis.webloans.domain.LoanExtension;
import lv.iljakorneckis.webloans.domain.LoanRiskAssessment;
import lv.iljakorneckis.webloans.enums.RiskStatus;
import lv.iljakorneckis.webloans.exceptions.RiskAssessmentException;
import lv.iljakorneckis.webloans.repository.LoanRepository;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@Transactional
public class LoanServiceImpl implements LoanService {

    @Autowired
    private LoanRiskAssessor riskAssessor;

    @Autowired
    private LoanRepository loanRepo;

    @Override
    public Loan applyForLoan(LoanApplication application) throws RiskAssessmentException {
        LoanRiskAssessment assessment = riskAssessor.assessRisk(application);

        if(assessment.getStatus() != RiskStatus.OK) {
            throw new RiskAssessmentException(assessment.getStatus(), assessment.getMessage());
        }

        Loan loan = new Loan();
        loan.setUserId(application.getUserId());
        loan.setAmount(application.getAmount());
        loan.setEndDate(application.getTerm());
        loan.setApplicationDate(application.getApplicationDate());
        loan.setInterest(BigDecimal.valueOf(1.5).setScale(2, RoundingMode.HALF_UP));

        return loanRepo.save(loan);
    }

    @Override
    public Loan extendLoan(Long loanId, String userId) {
        Loan loan = loanRepo.findByIdAndUserId(loanId, userId);

        LoanExtension extension = new LoanExtension();
        extension.setExtensionDate(DateTime.now());

        loan.setEndDate(loan.getEndDate().plusWeeks(1));
        loan.setInterest(loan.getInterest().multiply(BigDecimal.valueOf(1.5).setScale(2, RoundingMode.HALF_UP)));
        loan.getExtensionHistrory().add(extension);

        return loanRepo.save(loan);
    }

    @Override
    public List<Loan> getLoanHistory(String userId) {
        return loanRepo.findByUserId(userId);
    }
}