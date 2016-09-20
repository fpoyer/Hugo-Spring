package hugo.weaving.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.CodeSignature;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import hugo.weaving.internal.Hugo;

public class HugoTest {
	private static final String PARAM_VALUE2 = "paramValue2";
	private static final String PARAM_VALUE1 = "paramValue1";
	private static final String PARAM_NAME2 = "paramName2";
	private static final String PARAM_NAME1 = "paramName1";
	private static final Object[] PARAMETER_VALUES = new Object[] { PARAM_VALUE1, PARAM_VALUE2 };
	private static final String[] PARAMETER_NAMES = new String[] { PARAM_NAME1, PARAM_NAME2 };
	private static final String SIGNATURE_NAME = "signatureName";
	private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
	private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
	private PrintStream originalOut;
	private PrintStream originalErr;
	private Hugo hugo;

	@Mock
	private static ProceedingJoinPoint joinPoint;
	@Mock
	private static CodeSignature signature;

	@Rule
	public MockitoRule mockitoRule = MockitoJUnit.rule();

	@Before
	public void setUpStreams() {
		originalOut = System.out;
		originalErr = System.err;
		System.setOut(new PrintStream(outContent));
		System.setErr(new PrintStream(errContent));
	}

	@Before
	public void setUpHugoInstance() {
		Hugo.setEnabled(true);
		hugo = new Hugo();
	}

	@After
	public void cleanUpStreams() {
		System.setOut(originalOut);
		System.setErr(originalErr);
	}

	@Test
	public void whenDisabledThenHugoLogsNothing() throws Throwable {
		Hugo.setEnabled(false);
		try {
			hugo.logAndExecute(joinPoint);
			assertThat(outContent.toString()).isEmpty();
			assertThat(errContent.toString()).isEmpty();
		} finally {
			Hugo.setEnabled(true);
		}
	}

	@Test
	public void whenEnabledThenHugoLogsNothing() throws Throwable {
		when(joinPoint.getSignature()).thenReturn(signature);
		when(signature.getDeclaringType()).thenReturn(HugoTest.class);
		when(signature.getName()).thenReturn(SIGNATURE_NAME);
		when(signature.getParameterNames()).thenReturn(PARAMETER_NAMES);
		when(joinPoint.getArgs()).thenReturn(PARAMETER_VALUES);

		hugo.logAndExecute(joinPoint);

		StringBuilder builder = new StringBuilder();
		builder.append(HugoTest.class.getSimpleName()).append(" : \u21E2 ").append(SIGNATURE_NAME).append('(')
				.append(PARAM_NAME1).append("=\"").append(PARAM_VALUE1).append("\", ").append(PARAM_NAME2).append("=\"")
				.append(PARAM_VALUE2).append("\")\n").append(HugoTest.class.getSimpleName()).append(" : \u21E0 ").append(SIGNATURE_NAME).append(" [0ms]\n");
		String signatureAsString = builder.toString();
		
		assertThat(outContent.toString()).isEqualTo(signatureAsString);
		assertThat(errContent.toString()).isEmpty();
	}

}
