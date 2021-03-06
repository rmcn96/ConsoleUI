package components;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.lang.reflect.Type;

import main_console.GoalValues;
import main_console.IValues;
import messages.IMessage;
import messages.JSONMessage;
import messages.UIMessage;
import messages.ValueMessage;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class ParserComponent extends Component {
	private Component _sendComponent;
	private AtomicBoolean _stop;
	private ConcurrentLinkedQueue<JSONMessage> _inboundQueue;

	public ParserComponent(Component logger, Component console, Component callback) {
		super(logger, console);
		_sendComponent = callback;
		_stop = new AtomicBoolean(false);
		_inboundQueue = new ConcurrentLinkedQueue<JSONMessage>();
	}

	/**
	 * Parses the json string to a list of IValues
	 * 
	 * @param jsonString
	 *            String consistent of JSON, refer to specs for how to create
	 * @return List of IValue components
	 */
	private List<IValues> parse(String jsonString) {
		Type type = new TypeToken<Map<String, Object>>() {
		}.getType();
		Gson gson = new Gson();
		Map<String, Map<String, Map<String, Object>>> full = gson.fromJson(jsonString, type);
		List<IValues> list = new ArrayList<IValues>();
		for (String val : full.get("values").keySet()) { //Val should be an Index
			IValues ivals = mapToValues(full.get("values").get(val));
			list.add(Integer.parseInt(val), ivals);
		}
		return list;
	}

	private IValues mapToValues(Map<String, Object> map) {
		String name = (String) map.get("name");
		String current = (String) map.get("current");
		String month = (String) map.get("month");
		String goal = (String) map.get("goal");
		boolean goalMet = (boolean) map.get("met");
		return new GoalValues(name, current, month, goal, goalMet);
	}

	@Override
	public void send(IMessage message) {
		message.dispatch(this);
	}

	@Override
	public void start() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				runMessageProcesser();
			}
		}).start();
	}

	@Override
	public synchronized void handle(JSONMessage msg) {
		_inboundQueue.add(msg);
		notifyAll();
	}

	/**
	 * Runs a continuous loop, evaluating incoming messages and sending new
	 * ones.
	 */
	private synchronized void runMessageProcesser() {
		print("Started");
		while (!_stop.get()) {
			while (_inboundQueue.isEmpty()) {
				try {
					wait();
					if(_stop.get()){
						print("Stopped.");
						return;
					}
				} catch (InterruptedException e) {
					print("InterruptedException: " + e);
					log("InterruptedException: " + e);
				}
			}
			try {
				JSONMessage jsonMsg = _inboundQueue.poll();
				List<IValues> completed = parse(jsonMsg.getJSON());
				print("Parsed JSON data successfully.");
				log("Parsed JSON data successfully");
				IMessage uiMsg = new ValueMessage(this,
						jsonMsg.getCorrelationId(), completed, true);
				_sendComponent.send(uiMsg);
			} catch (Exception ex) {
				print(ex.getMessage());
				log(ex.getMessage());
			}
		}
	}

	@Override
	public synchronized void stop() {
		_stop.set(true);
		notifyAll();
	}

}