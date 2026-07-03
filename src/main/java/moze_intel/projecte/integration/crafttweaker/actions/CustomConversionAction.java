package moze_intel.projecte.integration.crafttweaker.actions;

import com.blamejared.crafttweaker.api.action.base.IUndoableAction;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import java.util.Iterator;
import java.util.Map;
import moze_intel.projecte.PECore;
import moze_intel.projecte.api.nss.NSSTag;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import moze_intel.projecte.integration.crafttweaker.mappers.CrTConversionEMCMapper;
import moze_intel.projecte.integration.crafttweaker.mappers.CrTConversionEMCMapper.CrTConversion;

public class CustomConversionAction implements IUndoableAction {

	private final CrTConversion conversion;

	public CustomConversionAction(NormalizedSimpleStack output, int amount, boolean propagateTags, boolean set, Map<NormalizedSimpleStack, Integer> ingredients) {
		//Note: We use an array map under the assumption that no conversion will have a massive number of ingredients
		// especially as copying into an array map via constructor is cheaper, and then we only iterate it in the mapper
		conversion = new CrTConversion(output, amount, propagateTags, set, new Object2IntArrayMap<>(ingredients));
	}

	@Override
	public void apply() {
		CrTConversionEMCMapper.addConversion(conversion);
	}

	@Override
	public String describe() {
		StringBuilder inputString = new StringBuilder();
		for (Iterator<Object2IntMap.Entry<NormalizedSimpleStack>> iterator = Object2IntMaps.fastIterator(conversion.ingredients()); iterator.hasNext(); ) {
			Object2IntMap.Entry<NormalizedSimpleStack> entry = iterator.next();
			if (!inputString.isEmpty()) {
				//If we already have elements, prepend a comma
				inputString.append(", ");
			}
			int amount = entry.getIntValue();
			if (amount > 1) {
				inputString.append(amount).append(" ");
			}
			inputString.append(entry.getKey());
		}
		String description = "Added custom conversion creating '" + conversion.amount() + "' of " + conversion.output() + ", from: " + inputString;
		if (conversion.propagateTags() && conversion.output() instanceof NSSTag) {
			description += "; propagating to elements of " + conversion.output();
		}
		return description;
	}

	@Override
	public void undo() {
		CrTConversionEMCMapper.removeConversion(conversion);
	}

	@Override
	public String describeUndo() {
		return "Undoing adding of custom conversion creating '" + conversion.amount() + "' of " + conversion.output();
	}

	@Override
	public String systemName() {
		return PECore.MODNAME;
	}
}