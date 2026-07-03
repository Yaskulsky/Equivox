package moze_intel.projecte.integration.crafttweaker.actions;

import com.blamejared.crafttweaker.api.action.base.IUndoableAction;
import com.blamejared.crafttweaker.natives.block.ExpandBlock;
import com.blamejared.crafttweaker.natives.block.ExpandBlockState;
import moze_intel.projecte.PECore;
import moze_intel.projecte.api.world_transmutation.IWorldTransmutation;
import moze_intel.projecte.api.world_transmutation.SimpleWorldTransmutation;
import moze_intel.projecte.api.world_transmutation.WorldTransmutation;
import moze_intel.projecte.world_transmutation.WorldTransmutationManager;

public class WorldTransmuteAction implements IUndoableAction {

	protected final IWorldTransmutation transmutation;
	private final boolean add;

	public WorldTransmuteAction(IWorldTransmutation transmutation, boolean add) {
		this.transmutation = transmutation;
		this.add = add;
	}

	@Override
	public String systemName() {
		return PECore.MODNAME;
	}

	@Override
	public void apply() {
		apply(add);
	}

	@Override
	public void undo() {
		apply(!add);
	}

	private void apply(boolean add) {
		if (add) {
			WorldTransmutationManager.INSTANCE.register(transmutation);
		} else {
			WorldTransmutationManager.INSTANCE.removeWorldTransmutation(transmutation);
		}
	}

	private String describeTransmutation() {
		return switch (transmutation) {
			case SimpleWorldTransmutation simple -> {
				String representation = ExpandBlock.getCommandString(simple.origin().value()) + " with output: " +
										ExpandBlock.getCommandString(simple.result().value());
				if (simple.hasAlternate()) {
					representation += " and secondary output: " + ExpandBlock.getCommandString(simple.altResult().value());
				}
				yield representation;
			}
			case WorldTransmutation worldTransmutation -> {
				String representation = ExpandBlockState.getCommandString(worldTransmutation.originState()) + " with output: " +
										ExpandBlockState.getCommandString(worldTransmutation.result());
				if (worldTransmutation.hasAlternate()) {
					representation += " and secondary output: " + ExpandBlockState.getCommandString(worldTransmutation.altResult());
				}
				yield representation;
			}
		};
	}

	@Override
	public String describe() {
		return (add ? "Adding" : "Removing") + " world transmutation recipe for: " + describeTransmutation();
	}

	@Override
	public String describeUndo() {
		return "Undoing " + (add ? "addition" : "removal") + " of world transmutation recipe for: " + describeTransmutation();
	}

	public static class RemoveAll implements IUndoableAction {

		@Override
		public void apply() {
			WorldTransmutationManager.INSTANCE.clearTransmutations();
		}

		@Override
		public String describe() {
			return "Removing all world transmutation recipes";
		}

		@Override
		public void undo() {
			WorldTransmutationManager.INSTANCE.resetWorldTransmutations();
		}

		@Override
		public String describeUndo() {
			return "Restored world transmutation recipes to default";
		}

		@Override
		public String systemName() {
			return PECore.MODNAME;
		}
	}
}