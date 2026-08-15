// pages/recipe-edit/recipe-edit.js
const api = require('../../api/index');

let tmpCounter = 1;
const nextTmp = () => `t${tmpCounter++}`;

Page({
  data: {
    recipeId: null,
    form: {
      name: '',
      description: '',
      visibility: 'PRIVATE',
      servings: 2,
      blocks: [
        { tmpId: nextTmp(), name: '主料', blockType: 'SINGLE', sortOrder: 0, ingredients: [] }
      ]
    },
    ingredientOptions: [],
    visibilityOptions: ['私有', '公开'],
    visibilityIndex: 0
  },

  onLoad(query) {
    if (query.id) {
      this.setData({ recipeId: Number(query.id) });
      wx.setNavigationBarTitle({ title: '编辑食谱' });
      this.loadDetail(query.id);
    } else {
      wx.setNavigationBarTitle({ title: '新建食谱' });
    }
    this.loadIngredients();
  },

  async loadDetail(id) {
    try {
      const r = await api.recipeDetail(id);
      const blocks = (r.blocks || []).map((b) => ({
        tmpId: nextTmp(),
        id: b.id,
        name: b.name,
        blockType: b.blockType,
        sortOrder: b.sortOrder,
        ingredients: (b.ingredients || []).map((i) => ({
          tmpId: nextTmp(),
          id: i.id,
          ingredientId: i.ingredientId,
          ingredientName: i.ingredientName,
          category: i.category,
          amountG: i.amountG == null ? '' : String(i.amountG),
          isAnchor: i.isAnchor || 0,
          note: i.note || ''
        }))
      }));
      this.setData({
        form: {
          name: r.name || '',
          description: r.description || '',
          visibility: r.visibility || 'PRIVATE',
          servings: r.servings || 2,
          blocks: blocks.length ? blocks : [{ tmpId: nextTmp(), name: '主料', blockType: 'SINGLE', sortOrder: 0, ingredients: [] }]
        },
        visibilityIndex: r.visibility === 'PUBLIC' ? 1 : 0
      });
    } catch (e) {
      console.error('load recipe failed', e);
    }
  },

  async loadIngredients() {
    try {
      const page = await api.ingredientPage({ page: 1, size: 200 });
      this.setData({ ingredientOptions: page.records || [] });
    } catch (e) { /* ignore */ }
  },

  onField(e) {
    const { key } = e.currentTarget.dataset;
    this.setData({ [`form.${key}`]: e.detail.value });
  },

  onPickVisibility(e) {
    const idx = Number(e.detail.value);
    this.setData({
      visibilityIndex: idx,
      'form.visibility': idx === 1 ? 'PUBLIC' : 'PRIVATE'
    });
  },

  onPickServings() {
    wx.showModal({
      title: '份数',
      editable: true,
      placeholderText: '请输入份数',
      success: (res) => {
        if (res.confirm && res.content) {
          const n = parseInt(res.content, 10);
          if (!Number.isNaN(n) && n > 0) {
            this.setData({ 'form.servings': n });
          }
        }
      }
    });
  },

  onBlockField(e) {
    const { tmp, key } = e.currentTarget.dataset;
    const blocks = this.data.form.blocks.map((b) =>
      b.tmpId === tmp ? { ...b, [key]: e.detail.value } : b
    );
    this.setData({ 'form.blocks': blocks });
  },

  onAddBlock() {
    const blocks = [...this.data.form.blocks, {
      tmpId: nextTmp(),
      name: '辅料',
      blockType: 'SINGLE',
      sortOrder: this.data.form.blocks.length,
      ingredients: []
    }];
    this.setData({ 'form.blocks': blocks });
  },

  onRemoveBlock(e) {
    const { tmp } = e.currentTarget.dataset;
    const blocks = this.data.form.blocks.filter((b) => b.tmpId !== tmp);
    this.setData({ 'form.blocks': blocks.length ? blocks : [{ tmpId: nextTmp(), name: '主料', blockType: 'SINGLE', sortOrder: 0, ingredients: [] }] });
  },

  onAddIngredient(e) {
    const { tmp } = e.currentTarget.dataset;
    const blocks = this.data.form.blocks.map((b) =>
      b.tmpId === tmp
        ? { ...b, ingredients: [...b.ingredients, { tmpId: nextTmp(), ingredientId: null, ingredientName: '', amountG: '', isAnchor: 0, note: '' }] }
        : b
    );
    this.setData({ 'form.blocks': blocks });
  },

  onRemoveIngredient(e) {
    const { btmp, itmp } = e.currentTarget.dataset;
    const blocks = this.data.form.blocks.map((b) =>
      b.tmpId === btmp ? { ...b, ingredients: b.ingredients.filter((i) => i.tmpId !== itmp) } : b
    );
    this.setData({ 'form.blocks': blocks });
  },

  onIngredientField(e) {
    const { btmp, itmp, key } = e.currentTarget.dataset;
    const blocks = this.data.form.blocks.map((b) => {
      if (b.tmpId !== btmp) return b;
      return {
        ...b,
        ingredients: b.ingredients.map((i) =>
          i.tmpId === itmp ? { ...i, [key]: e.detail.value } : i
        )
      };
    });
    this.setData({ 'form.blocks': blocks });
  },

  onPickIngredient(e) {
    const { btmp, itmp } = e.currentTarget.dataset;
    const opts = this.data.ingredientOptions;
    if (!opts.length) {
      wx.showToast({ title: '暂无食材', icon: 'none' });
      return;
    }
    wx.showActionSheet({
      itemList: opts.map((o) => `${o.name} (${o.category})`),
      success: (res) => {
        const ing = opts[res.tapIndex];
        const blocks = this.data.form.blocks.map((b) => {
          if (b.tmpId !== btmp) return b;
          return {
            ...b,
            ingredients: b.ingredients.map((i) =>
              i.tmpId === itmp ? { ...i, ingredientId: ing.id, ingredientName: ing.name, category: ing.category } : i
            )
          };
        });
        this.setData({ 'form.blocks': blocks });
      }
    });
  },

  buildPayload() {
    const { form, recipeId } = this.data;
    return {
      name: form.name.trim(),
      description: form.description || '',
      visibility: form.visibility,
      servings: Number(form.servings) || 1,
      blocks: form.blocks.map((b, idx) => ({
        name: b.name || '主料',
        blockType: b.blockType || 'SINGLE',
        sortOrder: b.sortOrder == null ? idx : b.sortOrder,
        ingredients: b.ingredients
          .filter((i) => i.ingredientId && i.amountG)
          .map((i) => ({
            ingredientId: i.ingredientId,
            amountG: Number(i.amountG),
            isAnchor: i.isAnchor || 0,
            note: i.note || ''
          }))
      })),
      tagIds: []
    };
  },

  async onSave() {
    const payload = this.buildPayload();
    if (!payload.name) {
      wx.showToast({ title: '请输入食谱名', icon: 'none' });
      return;
    }
    const allIngredients = payload.blocks.flatMap((b) => b.ingredients);
    if (allIngredients.length === 0) {
      wx.showToast({ title: '请至少添加 1 个食材', icon: 'none' });
      return;
    }
    try {
      if (this.data.recipeId) {
        await api.recipeUpdate(this.data.recipeId, payload);
        wx.showToast({ title: '已保存' });
      } else {
        await api.recipeCreate(payload);
        wx.showToast({ title: '已创建' });
      }
      setTimeout(() => wx.navigateBack(), 600);
    } catch (e) {
      console.error('save recipe failed', e);
    }
  },

  onDelete() {
    wx.showModal({
      title: '确认删除',
      content: '删除后不可恢复',
      success: async (res) => {
        if (res.confirm && this.data.recipeId) {
          try {
            await api.recipeDelete(this.data.recipeId);
            wx.showToast({ title: '已删除' });
            setTimeout(() => wx.navigateBack(), 600);
          } catch (e) { /* ignore */ }
        }
      }
    });
  }
});
