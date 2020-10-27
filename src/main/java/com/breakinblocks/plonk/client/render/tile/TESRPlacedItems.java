package com.breakinblocks.plonk.client.render.tile;

import com.breakinblocks.plonk.common.block.BlockPlacedItems;
import com.breakinblocks.plonk.common.tile.TilePlacedItems;
import com.breakinblocks.plonk.common.tile.TilePlacedItems.ItemMeta;
import com.breakinblocks.plonk.common.util.MatrixUtils;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.ItemFrameRenderer;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.item.ItemFrameEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.DirectionProperty;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.common.model.TransformationHelper;

import java.util.Objects;

import static com.breakinblocks.plonk.common.tile.TilePlacedItems.RENDER_TYPE_BLOCK;
import static com.breakinblocks.plonk.common.tile.TilePlacedItems.RENDER_TYPE_ITEM;

public class TESRPlacedItems extends TileEntityRenderer<TilePlacedItems> {

    public static final DirectionProperty FACING = BlockPlacedItems.FACING;
    private static final Minecraft mc = Minecraft.getInstance();
    private static final ItemRenderer itemRenderer = mc.getItemRenderer();

    private static final Tessellator tessellator = Tessellator.getInstance();
    private static final BufferBuilder bufferbuilder = tessellator.getBuffer();
    private static final BlockRendererDispatcher blockRendererDispatcher = mc.getBlockRendererDispatcher();

    public TESRPlacedItems(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
    }

    public static int getRenderTypeFromStack(ItemStack itemstack) {
        IBakedModel model = itemRenderer.getItemModelWithOverrides(itemstack, null, null);
        Matrix4f matrixFixed = TransformationHelper.toTransformation(model.getItemCameraTransforms().getTransform(ItemCameraTransforms.TransformType.FIXED)).getMatrix();
        Matrix4f matrixGui = TransformationHelper.toTransformation(model.getItemCameraTransforms().getTransform(ItemCameraTransforms.TransformType.GUI)).getMatrix();
        Matrix4f difference = MatrixUtils.difference(matrixFixed, matrixGui);
        MatrixUtils.TransformData transform = new MatrixUtils.TransformData(difference);

        // Check difference between item frame vs how it's displayed in the inventory to see?
        // Scaling Factor
        double hS = Math.abs((transform.sx + transform.sy + transform.sz) / 3.0);
        // Rotation angles
        double hRotP = (360 + transform.pitch) % 90;
        hRotP = Math.min(Math.abs(hRotP), Math.abs(hRotP - 90));
        double hRotY = (360 + transform.yaw) % 90;
        hRotY = Math.min(Math.abs(hRotY), Math.abs(hRotY - 90));
        double hRotR = (360 + transform.roll) % 90;
        hRotR = Math.min(Math.abs(hRotR), Math.abs(hRotR - 90));
        double hRot = hRotP + hRotY + hRotR;
        //String message = String.format("hS=%.3f hRot=%.3f\n", hS, hRot) + transform.toString();
        //for (String line : message.split("\r?\n"))
        //    Minecraft.getInstance().ingameGUI.getChatGUI().printChatMessage(new StringTextComponent(line));
        // Change in scaling is < 1 and change in rotation at non-right angles;
        return hS < (1.0 - 0.001) && hRot > 0.001 ? RENDER_TYPE_BLOCK : RENDER_TYPE_ITEM;
    }

    @Override
    public void render(TilePlacedItems tileEntityIn, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
        Direction facing = Objects.requireNonNull(tileEntityIn.getWorld()).getBlockState(tileEntityIn.getPos()).get(FACING);

        matrixStackIn.push();
        // Centre of Block
        //matrixStackIn.translate(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
        matrixStackIn.translate(0.5, 0.5, 0.5);

        //Rotate Facing
        switch (facing) {
            case UP: // DOWN
                break;
            case DOWN: // UP
                //GL11.glRotated(180, 1.0, 0.0, 0.0);
                //GL11.glRotated(180, 0.0, 1.0, 0.0);
                matrixStackIn.rotate(Vector3f.XP.rotationDegrees(180));
                matrixStackIn.rotate(Vector3f.YP.rotationDegrees(180));
                break;
            case SOUTH: // NORTH
                //GL11.glRotated(90, 1.0, 0.0, 0.0);
                matrixStackIn.rotate(Vector3f.XP.rotationDegrees(90));
                break;
            case NORTH: // SOUTH
                //GL11.glRotated(90, 1.0, 0.0, 0.0);
                //GL11.glRotated(180, 0.0, 0.0, 1.0);
                matrixStackIn.rotate(Vector3f.XP.rotationDegrees(90));
                matrixStackIn.rotate(Vector3f.ZP.rotationDegrees(180));
                break;
            case EAST: // WEST
                //GL11.glRotated(90, 1.0, 0.0, 0.0);
                //GL11.glRotated(-90, 0.0, 0.0, 1.0);
                matrixStackIn.rotate(Vector3f.XP.rotationDegrees(90));
                matrixStackIn.rotate(Vector3f.ZP.rotationDegrees(-90));
                break;
            case WEST: // EAST
                //GL11.glRotated(90, 1.0, 0.0, 0.0);
                //GL11.glRotated(90, 0.0, 0.0, 1.0);
                matrixStackIn.rotate(Vector3f.XP.rotationDegrees(90));
                matrixStackIn.rotate(Vector3f.ZP.rotationDegrees(90));
                break;
        }

        matrixStackIn.translate(0.0, -0.5, 0.0);

        //Rotate about axis
        matrixStackIn.rotate(new Quaternion(Vector3f.YP, (float) -tileEntityIn.getTileRotationAngle(), true));

        ItemStack[] contents = tileEntityIn.getContentsDisplay();
        ItemMeta[] contentsMeta = tileEntityIn.getContentsMeta();
        int num = contents.length;

        if (num > 0) {
            boolean halfSize = num > 1;
            for (int slot = 0; slot < num; slot++) {
                ItemStack stack = tileEntityIn.getStackInSlot(slot);
                if (stack.isEmpty()) continue;
                matrixStackIn.push();
                switch (num) {
                    case 1:
                        // No shift
                        break;
                    case 2:
                        // Shift left for slot 0
                        if (slot == 0) {
                            matrixStackIn.translate(-0.25, 0.0, 0.0);
                        } else {
                            matrixStackIn.translate(0.25, 0.0, 0.0);
                        }
                        break;
                    default:
                        boolean left = slot % 2 == 0;
                        boolean top = (slot / 2) == 0;
                        matrixStackIn.translate(left ? -0.25 : 0.25, 0.0, top ? -0.25 : 0.25);
                        break;
                }
                //renderStack(world, stack, partialTicks, halfSize, world.rand.nextBoolean());
                renderStack(partialTicks, matrixStackIn, bufferIn, combinedLightIn, stack, contentsMeta[slot], halfSize);
                matrixStackIn.pop();
            }
        }

        matrixStackIn.pop();
    }

    /**
     * Render item at location facing up
     * Refer to ItemFrame rendering
     *
     * @param partialTicks    fractional tick
     * @param matrixStackIn   transformation matrix stack
     * @param bufferIn        render type buffer
     * @param combinedLightIn Block and Sky light combined
     * @param stack           ItemStack to render
     * @param meta            Metadata for the stack
     * @param halfSize        If items should be rendered at half size (blocks are always rendered half size)
     * @see ItemFrameRenderer#render(ItemFrameEntity, float, float, MatrixStack, IRenderTypeBuffer, int)
     */
    public void renderStack(float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, ItemStack stack, ItemMeta meta, boolean halfSize) {
        if (stack.isEmpty()) return;
        matrixStackIn.push();
        GlStateManager.disableLighting();
        GlStateManager.pushLightingAttributes();
        RenderHelper.enableStandardItemLighting();

        // Rotate item
        //GL11.glRotated(-meta.getRotationAngle(), 0.0, 1.0, 0.0);
        matrixStackIn.rotate(new Quaternion(new Vector3f(0f, 1f, 0f), (float) -meta.getRotationAngle(), true));

        // GROUND
        //matrixStackIn.translate(0f, -0.125f, 0f);
        //matrixStackIn.scale(2F, 2F, 2F);

        // FIXED
        //GlStateManager.rotate(180f, 0f, 1f, 0f);
        matrixStackIn.rotate(new Quaternion(new Vector3f(0f, 1f, 0f), 180, true));
        switch (meta.renderType) {
            case RENDER_TYPE_BLOCK: {
                matrixStackIn.translate(0f, 0.25f, 0f);

                itemRenderer.renderItem(stack, ItemCameraTransforms.TransformType.FIXED, combinedLightIn, OverlayTexture.NO_OVERLAY, matrixStackIn, bufferIn);
            }
            break;
            case RENDER_TYPE_ITEM:
            default:
                matrixStackIn.translate(0f, 2f / 48, 0f);
                //GlStateManager.rotate(90f, 1f, 0f, 0f);
                matrixStackIn.rotate(new Quaternion(new Vector3f(1f, 0f, 0f), 90, true));
                if (halfSize)
                    matrixStackIn.scale(0.5F, 0.5F, 0.5F);
                itemRenderer.renderItem(stack, ItemCameraTransforms.TransformType.FIXED, combinedLightIn, OverlayTexture.NO_OVERLAY, matrixStackIn, bufferIn);
        }

        RenderHelper.disableStandardItemLighting();
        GlStateManager.popAttributes();
        GlStateManager.enableLighting();
        matrixStackIn.pop();
    }
}
